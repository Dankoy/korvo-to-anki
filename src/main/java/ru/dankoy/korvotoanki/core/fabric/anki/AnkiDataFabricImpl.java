package ru.dankoy.korvotoanki.core.fabric.anki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;
import ru.dankoy.korvotoanki.core.domain.anki.Definition;
import ru.dankoy.korvotoanki.core.domain.anki.Meaning;
import ru.dankoy.korvotoanki.core.domain.dictionaryapi.Phonetics;
import ru.dankoy.korvotoanki.core.domain.dictionaryapi.Word;
import ru.dankoy.korvotoanki.core.domain.freedic.FreeDictionaryEntry;
import ru.dankoy.korvotoanki.core.domain.freedic.FreeDictionarySense;
import ru.dankoy.korvotoanki.core.domain.freedic.FreeDictionaryWord;
import ru.dankoy.korvotoanki.core.domain.freedic.Pronunciation;
import ru.dankoy.korvotoanki.core.domain.googletranslation.GoogleTranslation;
import ru.dankoy.korvotoanki.core.domain.googletranslation.Synonym;

@Component
public class AnkiDataFabricImpl implements AnkiDataFabric {

  private static final long MAX_EXAMPLES = 20;
  private static final long MAX_SYNONYMS = 20;

  @Override
  public AnkiData createAnkiData(
      Vocabulary vocabulary,
      GoogleTranslation googleTranslation,
      List<Word> words,
      FreeDictionaryWord freeDictionaryWord) {

    var myExample = obtainVocabularyExample(vocabulary);

    return AnkiData.builder()
        .word(vocabulary.word())
        .book(vocabulary.title().name())
        .translations(googleTranslation.getTranslations()) // could be empty list
        .transcription(chooseTranscription(googleTranslation, words, freeDictionaryWord))
        .myExample(myExample)
        .meanings(mergeMeanings(googleTranslation, words, freeDictionaryWord))
        .examples(mergeExamples(googleTranslation, words, freeDictionaryWord, myExample))
        .build();
  }

  private String obtainVocabularyExample(Vocabulary vocabulary) {

    if (Objects.nonNull(vocabulary.prevContext())) {
      return vocabulary.prevContext() + vocabulary.word() + vocabulary.nextContext();
    } else {
      return null;
    }
  }

  // freedictionaryapi transcription has the highest priority - it's derived from wiktionary
  // ipa transcriptions. Then dictionaryapi.dev phonetic string, then its phonetics texts, then
  // google translate transcription.
  private String chooseTranscription(
      GoogleTranslation googleTranslation,
      List<Word> words,
      FreeDictionaryWord freeDictionaryWord) {

    var freeDictionaryTranscription = freeDictionaryTranscription(freeDictionaryWord);

    if (freeDictionaryTranscription.isPresent()) {
      return freeDictionaryTranscription.get();
    }

    var dictionaryApiTranscription =
        words.stream().map(Word::getPhonetic).filter(Objects::nonNull).findFirst();

    if (dictionaryApiTranscription.isEmpty()) {

      // get first non-empty or null transcription
      var dictionaryApiTranscriptionOptional =
          words.stream()
              .map(Word::getPhonetics)
              .filter(Objects::nonNull)
              .flatMap(List::stream)
              .map(Phonetics::getText)
              .filter(Objects::nonNull)
              .findFirst();

      // either dictionaryapi transcription or Google Translate transcription
      return dictionaryApiTranscriptionOptional.orElse(googleTranslation.getTranscription());

    } else {
      return dictionaryApiTranscription.get();
    }
  }

  private Optional<String> freeDictionaryTranscription(FreeDictionaryWord freeDictionaryWord) {

    if (Objects.isNull(freeDictionaryWord) || Objects.isNull(freeDictionaryWord.getEntries())) {
      return Optional.empty();
    }

    return freeDictionaryWord.getEntries().stream()
        .filter(entry -> Objects.nonNull(entry.getPronunciations()))
        .flatMap(entry -> entry.getPronunciations().stream())
        .map(Pronunciation::getText)
        .filter(Objects::nonNull)
        .filter(text -> !text.isBlank())
        .findFirst();
  }

  // dictionaryapi meanings are the base. Google translation definitions of the same part of
  // speech are appended to them with their examples and per-definition synonyms. Google
  // translation definitions that are exact duplicates of a dictionaryapi definition (same info)
  // are merged instead of added: their synonyms are unioned into the dictionaryapi definition
  // and their example is used when the dictionaryapi definition has none. Google translation
  // synonyms of the same part of speech are unioned into the meaning level synonyms, the list
  // stays capped so the card doesn't become overloaded. Meanings of parts of speech that only
  // google translation has are added on top.
  private List<Meaning> mergeMeanings(
      GoogleTranslation googleTranslation,
      List<Word> words,
      FreeDictionaryWord freeDictionaryWord) {

    List<Meaning> dictionaryApiMeanings = createMeaningsFromDictionaryApi(words);
    List<Meaning> googleMeanings = createMeaningsFromGoogleTranslation(googleTranslation);

    if (dictionaryApiMeanings.isEmpty()) {
      return mergeFreeDictionaryMeanings(googleMeanings, freeDictionaryWord);
    }

    Map<String, Meaning> googleMeaningsByType =
        googleMeanings.stream()
            .collect(Collectors.toMap(Meaning::type, m -> m, (m1, m2) -> m1, HashMap::new));

    Set<String> dictionaryApiTypes =
        dictionaryApiMeanings.stream().map(Meaning::type).collect(Collectors.toSet());

    List<Meaning> result = new ArrayList<>();

    for (Meaning dictionaryApiMeaning : dictionaryApiMeanings) {

      Meaning googleMeaning = googleMeaningsByType.get(dictionaryApiMeaning.type());

      List<Definition> definitions = new ArrayList<>(dictionaryApiMeaning.definitions());
      List<String> synonyms = dictionaryApiMeaning.synonyms();

      if (Objects.nonNull(googleMeaning)) {
        mergeDefinitions(definitions, googleMeaning.definitions());
        synonyms = mergeSynonyms(synonyms, googleMeaning.synonyms());
      }

      result.add(
          new Meaning(
              dictionaryApiMeaning.type(), definitions, synonyms, dictionaryApiMeaning.antonyms()));
    }

    // parts of speech that only google translation has
    googleMeanings.stream()
        .filter(googleMeaning -> !dictionaryApiTypes.contains(googleMeaning.type()))
        .forEach(result::add);

    return mergeFreeDictionaryMeanings(result, freeDictionaryWord);
  }

  // freedictionaryapi (wiktionary) entries are merged on top of what dictionaryapi and google
  // translation produced. Senses (including subsenses, flattened) are appended to the definitions
  // of the same part of speech - exact duplicates (same info) are merged instead: union of
  // synonyms and antonyms and the wiktionary example when the existing definition has none. The
  // first sense example becomes the definition example, the rest go to the card examples section.
  // Entry level synonyms and antonyms are unioned into the meaning level lists (capped). Parts of
  // speech that only freedictionaryapi has are added on top.
  private List<Meaning> mergeFreeDictionaryMeanings(
      List<Meaning> meanings, FreeDictionaryWord freeDictionaryWord) {

    if (Objects.isNull(freeDictionaryWord)
        || Objects.isNull(freeDictionaryWord.getEntries())
        || freeDictionaryWord.getEntries().isEmpty()) {
      return meanings;
    }

    List<Meaning> result = new ArrayList<>(meanings);

    Map<String, Integer> indexByType = new HashMap<>();
    for (int i = 0; i < result.size(); i++) {
      indexByType.putIfAbsent(result.get(i).type(), i);
    }

    for (FreeDictionaryEntry entry : freeDictionaryWord.getEntries()) {

      var type = entry.getPartOfSpeech();
      List<Definition> freeDictionaryDefinitions = createDefinitionsFromFreeDictionaryEntry(entry);

      Integer index = indexByType.get(type);

      if (Objects.nonNull(index)) {

        Meaning existing = result.get(index);

        List<Definition> definitions = new ArrayList<>(existing.definitions());
        mergeDefinitions(definitions, freeDictionaryDefinitions);

        result.set(
            index,
            new Meaning(
                type,
                definitions,
                mergeSynonyms(existing.synonyms(), entry.getSynonyms()),
                mergeSynonyms(existing.antonyms(), entry.getAntonyms())));

      } else {

        indexByType.put(type, result.size());
        result.add(
            new Meaning(
                type,
                freeDictionaryDefinitions,
                capSynonyms(entry.getSynonyms()),
                capSynonyms(entry.getAntonyms())));
      }
    }

    return result;
  }

  // a sense becomes a definition with its first example, subsenses are flattened into the same
  // definition list right after their parent sense
  private List<Definition> createDefinitionsFromFreeDictionaryEntry(FreeDictionaryEntry entry) {

    List<Definition> result = new ArrayList<>();

    if (Objects.nonNull(entry.getSenses())) {
      entry.getSenses().forEach(sense -> appendFreeDictionarySense(result, sense));
    }

    return result;
  }

  private void appendFreeDictionarySense(List<Definition> result, FreeDictionarySense sense) {

    result.add(
        new Definition(
            sense.getDefinition(),
            firstFreeDictionaryExample(sense),
            capSynonyms(sense.getSynonyms()),
            capSynonyms(sense.getAntonyms())));

    if (Objects.nonNull(sense.getSubsenses())) {
      sense.getSubsenses().forEach(subsense -> appendFreeDictionarySense(result, subsense));
    }
  }

  // wiktionary examples come in several citation shapes:
  //   "1850s, Author, Work\nSentence" - year, optional circa marker ("c. 1824", "a. 1950"),
  //     month name ("August 1773") or bracketed year ("[1906] 2004") on the first line
  //   "Quote — Author, Work (Publisher 2006, p. 1050)" - reference appended after the quote
  //   "1942: 'Quote'" - leading year prefix
  //   'Author (1877), "Work", in ... →OCLC: "Quote"' - full citation, quoted sentence is last
  // The reference is useless on the card, so it is dropped and only the sentence is kept.
  // Multi-line verses without a reference are kept whole. Whitespace is collapsed.
  private String cleanDictionaryExample(String example) {

    if (Objects.isNull(example)) {
      return null;
    }

    var result = example;

    var firstLineEnd = result.indexOf('\n');
    if (firstLineEnd > 0 && isQuotationReferenceLine(result.substring(0, firstLineEnd).trim())) {
      result = result.substring(firstLineEnd + 1);
    }

    result = result.replaceAll("\\s+", " ").trim();

    // trailing " — Author, Work (Publisher 2006, p. 1050)"
    var dash = result.lastIndexOf(" — ");
    if (dash > 0 && result.substring(dash).matches(" — .*\\(.*\\d{4}.*\\)")) {
      result = result.substring(0, dash).trim();
    }

    // leading "1942: " year prefix left by the trailing-reference cut
    result = result.replaceFirst("^\\d{3,4}s?:\\s+", "");

    // full citation whose quoted sentence is the last quoted span
    if ((result.contains("→OCLC") || result.contains("→ISBN"))
        && result.matches(".*\\(\\d{4}\\).*")) {
      var start = result.lastIndexOf('“');
      var end = result.lastIndexOf('”');
      if (start >= 0 && end > start) {
        result = result.substring(start + 1, end);
      }
    }

    return result;
  }

  // "1850s, Author, Work", "c. 1824 (note) Author, Work", "[1906] 2004, Work, Author tr.",
  // "August 1773, Author, Work"
  private boolean isQuotationReferenceLine(String line) {

    if (line.matches(
        "^(?:(?:c\\.|ca\\.|circa|a\\.|fl\\.)\\s+)?\\[?\\d{3,4}\\]?([-–]\\d{2,4})?s?([\\s,:].*)?$")) {
      return true;
    }

    return line.matches(
        "^(?:January|February|March|April|May|June|July|August|September|October|November|"
            + "December)\\s+(?:\\d{1,2}(?:st|nd|rd|th)?\\s*,\\s*)?\\d{3,4}s?([\\s,:].*)?$");
  }

  private String firstFreeDictionaryExample(FreeDictionarySense sense) {

    if (Objects.nonNull(sense.getExamples()) && !sense.getExamples().isEmpty()) {
      return cleanDictionaryExample(sense.getExamples().get(0));
    }

    return null;
  }

  // appends definitions after existing ones. Duplicates are merged instead: union of synonyms and
  // antonyms (capped, existing first) and the incoming example when the existing definition has
  // none. Definitions are considered duplicates when their texts are equal ignoring case and
  // trailing punctuation, or when they differ only by leading usage qualifiers in parentheses -
  // dictionaries repeat the same definition with and without tags like "(archaic)" or
  // "(rare, intransitive, perhaps nonstandard)".
  private void mergeDefinitions(List<Definition> definitions, List<Definition> newDefinitions) {

    for (Definition newDefinition : newDefinitions) {

      int duplicateIndex = -1;
      for (int i = 0; i < definitions.size(); i++) {
        if (isSameDefinition(definitions.get(i), newDefinition)) {
          duplicateIndex = i;
          break;
        }
      }

      if (duplicateIndex < 0) {
        definitions.add(newDefinition);
        continue;
      }

      Definition duplicate = definitions.get(duplicateIndex);
      String example =
          (Objects.isNull(duplicate.example()) || duplicate.example().isBlank())
              ? newDefinition.example()
              : duplicate.example();

      definitions.set(
          duplicateIndex,
          new Definition(
              duplicate.info(),
              example,
              mergeSynonyms(duplicate.synonyms(), newDefinition.synonyms()),
              mergeSynonyms(duplicate.antonyms(), newDefinition.antonyms())));
    }
  }

  private boolean isSameDefinition(Definition first, Definition second) {
    return Objects.equals(dedupKey(first.info()), dedupKey(second.info()));
  }

  // text without leading parenthesized usage qualifiers ("(archaic) ", "(rare, intransitive) "),
  // lowercased, trailing sentence punctuation stripped, whitespace collapsed
  private String dedupKey(String info) {

    String normalized = info.trim();

    while (normalized.startsWith("(")) {
      var end = normalized.indexOf(')');
      if (end < 0) {
        break;
      }
      normalized = normalized.substring(end + 1).trim();
    }

    return normalized.toLowerCase().replaceAll("[.!?]+$", "").replaceAll("\\s+", " ").trim();
  }

  // union of two synonym lists, dictionaryapi first, capped; null-safe, returns null when both
  // are null
  private List<String> mergeSynonyms(List<String> first, List<String> second) {

    if (Objects.isNull(first) || first.isEmpty()) {
      return capSynonyms(second);
    }

    if (Objects.isNull(second) || second.isEmpty()) {
      return first;
    }

    List<String> merged = new ArrayList<>(first);
    for (String synonym : second) {
      if (!merged.contains(synonym)) {
        merged.add(synonym);
      }
    }

    return capSynonyms(merged);
  }

  // dictionaryapi.dev returns a list of word entries - a word with several etymologies comes as
  // multiple entries that repeat parts of speech (e.g. ajar is verb+adjective+adverb and
  // verb+adverb). Meanings of the same part of speech from all entries are grouped into one
  // meaning: definitions are appended in encounter order (exact duplicates are merged),
  // synonyms and antonyms are unioned and capped.
  private List<Meaning> createMeaningsFromDictionaryApi(List<Word> words) {

    Map<String, Meaning> meaningsByType = new LinkedHashMap<>();

    for (Word daWord : words) {

      for (ru.dankoy.korvotoanki.core.domain.dictionaryapi.Meaning daMeaning :
          daWord.getMeanings()) {

        var partOfSpeech = daMeaning.getPartOfSpeech();

        var ankiDefs =
            daMeaning.getDefinitions().stream()
                .map(
                    d ->
                        new Definition(
                            d.getInfo(),
                            cleanDictionaryExample(d.getExample()),
                            capSynonyms(d.getSynonyms()),
                            capSynonyms(d.getAntonyms())))
                .toList();

        Meaning existing = meaningsByType.get(partOfSpeech);

        if (Objects.isNull(existing)) {
          meaningsByType.put(
              partOfSpeech,
              new Meaning(
                  partOfSpeech,
                  new ArrayList<>(ankiDefs),
                  capSynonyms(daMeaning.getSynonyms()),
                  capSynonyms(daMeaning.getAntonyms())));
        } else {
          List<Definition> definitions = new ArrayList<>(existing.definitions());
          mergeDefinitions(definitions, ankiDefs);
          meaningsByType.put(
              partOfSpeech,
              new Meaning(
                  partOfSpeech,
                  definitions,
                  mergeSynonyms(existing.synonyms(), daMeaning.getSynonyms()),
                  mergeSynonyms(existing.antonyms(), daMeaning.getAntonyms())));
        }
      }
    }

    return new ArrayList<>(meaningsByType.values());
  }

  // Google translation definitions are grouped by part of speech. Synonyms are bound to
  // definitions by google translate internal definition id. Synonyms without bound definition
  // (no id, or the bound definition is not in the data, e.g. when md option was not requested)
  // are added to meaning level synonyms of the same part of speech. Types that have only
  // synonyms and no definitions get a meaning without definitions. Synonym lists are capped
  // on both levels, so the card doesn't become overloaded.
  private List<Meaning> createMeaningsFromGoogleTranslation(GoogleTranslation googleTranslation) {

    List<Meaning> result = new ArrayList<>();

    Map<String, List<ru.dankoy.korvotoanki.core.domain.googletranslation.Definition>>
        definitionsByType =
            googleTranslation.getDefinitions().stream()
                .collect(
                    Collectors.groupingBy(
                        ru.dankoy.korvotoanki.core.domain.googletranslation.Definition::type));

    Set<String> definitionIds =
        googleTranslation.getDefinitions().stream()
            .map(ru.dankoy.korvotoanki.core.domain.googletranslation.Definition::definitionId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    Map<String, List<String>> synonymsByDefinitionId = new HashMap<>();
    Map<String, List<String>> unboundSynonymsByType = new HashMap<>();

    for (Synonym synonym : googleTranslation.getSynonyms()) {
      var bound =
          Objects.nonNull(synonym.definitionId())
              && !synonym.definitionId().isBlank()
              && definitionIds.contains(synonym.definitionId());
      if (bound) {
        synonymsByDefinitionId.merge(
            synonym.definitionId(),
            new ArrayList<>(synonym.synonyms()),
            (s1, s2) -> {
              List<String> merged = new ArrayList<>(s1);
              merged.addAll(s2);
              return merged;
            });
      } else {
        unboundSynonymsByType.merge(
            synonym.type(),
            new ArrayList<>(synonym.synonyms()),
            (s1, s2) -> {
              List<String> merged = new ArrayList<>(s1);
              merged.addAll(s2);
              return merged;
            });
      }
    }

    definitionsByType.forEach(
        (type, defs) ->
            result.add(
                new Meaning(
                    type,
                    defs.stream()
                        .map(
                            d ->
                                new Definition(
                                    d.info(),
                                    d.example(),
                                    capSynonyms(
                                        synonymsByDefinitionId.getOrDefault(
                                            d.definitionId(), new ArrayList<>())),
                                    new ArrayList<>()))
                        .toList(),
                    capSynonyms(unboundSynonymsByType.getOrDefault(type, new ArrayList<>())),
                    new ArrayList<>())));

    // types that have only synonyms and no definitions, e.g. when ss option was requested
    // without md
    unboundSynonymsByType.keySet().stream()
        .filter(type -> !definitionsByType.containsKey(type))
        .forEach(
            type ->
                result.add(
                    new Meaning(
                        type,
                        new ArrayList<>(),
                        capSynonyms(unboundSynonymsByType.get(type)),
                        new ArrayList<>())));

    return result;
  }

  // both dictionaryapi and google translate can return very long synonym lists
  private List<String> capSynonyms(List<String> synonyms) {

    if (Objects.isNull(synonyms)) {
      return null;
    }

    return synonyms.stream().limit(MAX_SYNONYMS).toList();
  }

  // Google translation examples are not bound to any part of speech, so they can't be mapped to
  // any meaning. They are always merged into the card regardless of the dictionaryapi result.
  // Freedictionaryapi sense examples that are not used as definition examples (only the first
  // sense example becomes one) go to the card examples section as well, after google translate
  // examples. Examples that duplicate an example already on the card (a definition example from
  // any dictionary, or the book example sentence) are skipped. Comparison is done on normalized
  // text (lowercase, no punctuation, single spaces), the list is capped because google translate
  // returns every example it has and the card becomes overloaded.
  private List<String> mergeExamples(
      GoogleTranslation googleTranslation,
      List<Word> words,
      FreeDictionaryWord freeDictionaryWord,
      String myExample) {

    // definition examples are cleaned the same way they are on the card, otherwise a card
    // example that duplicates a definition example (e.g. google translate and freedictionaryapi
    // quoting the same wiktionary sentence) would not be recognized as a duplicate
    Set<String> cardExamples =
        Stream.concat(
                words.stream()
                    .flatMap(w -> w.getMeanings().stream())
                    .flatMap(m -> m.getDefinitions().stream())
                    .map(ru.dankoy.korvotoanki.core.domain.dictionaryapi.Definition::getExample)
                    .map(this::cleanDictionaryExample),
                googleTranslation.getDefinitions().stream()
                    .map(ru.dankoy.korvotoanki.core.domain.googletranslation.Definition::example)
                    .map(this::cleanDictionaryExample))
            .filter(Objects::nonNull)
            .map(this::normalizeExample)
            .collect(Collectors.toSet());

    // first sense examples become definition examples, so they are already on the card
    Set<String> freeDictionaryDefinitionExamples =
        freeDictionaryDefinitionExamples(freeDictionaryWord);
    cardExamples.addAll(freeDictionaryDefinitionExamples);

    if (Objects.nonNull(myExample)) {
      cardExamples.add(normalizeExample(myExample));
    }

    Stream<String> googleExamples =
        googleTranslation.getExamples().stream()
            .filter(Objects::nonNull)
            .filter(example -> !cardExamples.contains(normalizeExample(example)));

    // sense examples except the first ones (they are definition examples now)
    Stream<String> freeDictionaryExamples =
        freeDictionaryCardExamples(freeDictionaryWord).stream()
            .filter(example -> !cardExamples.contains(normalizeExample(example)));

    return Stream.concat(googleExamples, freeDictionaryExamples).limit(MAX_EXAMPLES).toList();
  }

  // normalized first examples of all senses - they become definition examples when meanings are
  // merged, so they must not be repeated in the card examples section
  private Set<String> freeDictionaryDefinitionExamples(FreeDictionaryWord freeDictionaryWord) {

    return freeDictionarySenses(freeDictionaryWord)
        .map(this::firstFreeDictionaryExample)
        .filter(Objects::nonNull)
        .map(this::normalizeExample)
        .collect(Collectors.toSet());
  }

  // all sense examples except the first ones of each sense
  private List<String> freeDictionaryCardExamples(FreeDictionaryWord freeDictionaryWord) {

    return freeDictionarySenses(freeDictionaryWord)
        .flatMap(
            sense ->
                Objects.isNull(sense.getExamples())
                    ? Stream.empty()
                    : sense.getExamples().stream().skip(1))
        .filter(Objects::nonNull)
        .map(this::cleanDictionaryExample)
        .distinct()
        .toList();
  }

  private Stream<FreeDictionarySense> freeDictionarySenses(FreeDictionaryWord freeDictionaryWord) {

    if (Objects.isNull(freeDictionaryWord)
        || Objects.isNull(freeDictionaryWord.getEntries())
        || freeDictionaryWord.getEntries().isEmpty()) {
      return Stream.empty();
    }

    return freeDictionaryWord.getEntries().stream()
        .filter(entry -> Objects.nonNull(entry.getSenses()))
        .flatMap(entry -> entry.getSenses().stream())
        .flatMap(this::flattenSense);
  }

  private Stream<FreeDictionarySense> flattenSense(FreeDictionarySense sense) {

    if (Objects.isNull(sense.getSubsenses()) || sense.getSubsenses().isEmpty()) {
      return Stream.of(sense);
    }

    return Stream.concat(
        Stream.of(sense), sense.getSubsenses().stream().flatMap(this::flattenSense));
  }

  // lowercase, punctuation removed, whitespace collapsed to single spaces
  private String normalizeExample(String example) {

    return example.toLowerCase().replaceAll("\\p{P}+", "").replaceAll("\\s+", " ").trim();
  }
}
