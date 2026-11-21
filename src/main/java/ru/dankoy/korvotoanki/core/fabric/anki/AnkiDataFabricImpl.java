package ru.dankoy.korvotoanki.core.fabric.anki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import ru.dankoy.korvotoanki.core.domain.googletranslation.GoogleTranslation;
import ru.dankoy.korvotoanki.core.domain.googletranslation.Synonym;

@Component
public class AnkiDataFabricImpl implements AnkiDataFabric {

  private static final long MAX_EXAMPLES = 10;
  private static final long MAX_SYNONYMS = 15;

  @Override
  public AnkiData createAnkiData(
      Vocabulary vocabulary, GoogleTranslation googleTranslation, List<Word> words) {

    var myExample = obtainVocabularyExample(vocabulary);

    return AnkiData.builder()
        .word(vocabulary.word())
        .book(vocabulary.title().name())
        .translations(googleTranslation.getTranslations()) // could be empty list
        .transcription(chooseTranscription(googleTranslation, words))
        .myExample(myExample)
        .meanings(mergeMeanings(googleTranslation, words))
        .examples(mergeExamples(googleTranslation, words, myExample))
        .build();
  }

  private String obtainVocabularyExample(Vocabulary vocabulary) {

    if (Objects.nonNull(vocabulary.prevContext())) {
      return vocabulary.prevContext() + vocabulary.word() + vocabulary.nextContext();
    } else {
      return null;
    }
  }

  private String chooseTranscription(GoogleTranslation googleTranslation, List<Word> words) {

    var dictionaryApiTranscription =
        words.stream().map(Word::getPhonetic).filter(Objects::nonNull).findFirst();

    if (dictionaryApiTranscription.isEmpty()) {

      // get first non-empty or null transcription
      var dictionaryApiTranscriptionOptional =
          words.stream()
              .flatMap(w -> Stream.of(w.getPhonetics()))
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

  // dictionaryapi meanings are the base. Google translation definitions of the same part of
  // speech are appended to them with their examples and per-definition synonyms. Google
  // translation definitions that are exact duplicates of a dictionaryapi definition (same info)
  // are merged instead of added: their synonyms are unioned into the dictionaryapi definition
  // and their example is used when the dictionaryapi definition has none. Google translation
  // synonyms of the same part of speech are unioned into the meaning level synonyms, the list
  // stays capped so the card doesn't become overloaded. Meanings of parts of speech that only
  // google translation has are added on top.
  private List<Meaning> mergeMeanings(GoogleTranslation googleTranslation, List<Word> words) {

    List<Meaning> dictionaryApiMeanings = createMeaningsFromDictionaryApi(words);
    List<Meaning> googleMeanings = createMeaningsFromGoogleTranslation(googleTranslation);

    if (dictionaryApiMeanings.isEmpty()) {
      return googleMeanings;
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

    return result;
  }

  // appends google translation definitions after dictionaryapi ones. Exact duplicates (same
  // info) are merged instead: union of synonyms (capped, dictionaryapi first) and the google
  // translation example when the dictionaryapi definition has none.
  private void mergeDefinitions(List<Definition> definitions, List<Definition> googleDefinitions) {

    for (Definition googleDefinition : googleDefinitions) {

      int duplicateIndex = -1;
      for (int i = 0; i < definitions.size(); i++) {
        if (Objects.equals(definitions.get(i).info(), googleDefinition.info())) {
          duplicateIndex = i;
          break;
        }
      }

      if (duplicateIndex < 0) {
        definitions.add(googleDefinition);
        continue;
      }

      Definition duplicate = definitions.get(duplicateIndex);
      String example =
          (Objects.isNull(duplicate.example()) || duplicate.example().isBlank())
              ? googleDefinition.example()
              : duplicate.example();

      definitions.set(
          duplicateIndex,
          new Definition(
              duplicate.info(),
              example,
              mergeSynonyms(duplicate.synonyms(), googleDefinition.synonyms()),
              duplicate.antonyms()));
    }
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

  private List<Meaning> createMeaningsFromDictionaryApi(List<Word> words) {

    List<Meaning> result = new ArrayList<>();

    for (Word daWord : words) {

      for (ru.dankoy.korvotoanki.core.domain.dictionaryapi.Meaning daMeaning :
          daWord.getMeanings()) {

        var partOfSpeech = daMeaning.getPartOfSpeech();
        List<String> daSynonyms = daMeaning.getSynonyms();
        List<String> daAntonyms = daMeaning.getAntonyms();

        var ankiDefs =
            daMeaning.getDefinitions().stream()
                .map(
                    d ->
                        new Definition(
                            d.getInfo(),
                            d.getExample(),
                            capSynonyms(d.getSynonyms()),
                            capSynonyms(d.getAntonyms())))
                .toList();

        result.add(new Meaning(partOfSpeech, ankiDefs, capSynonyms(daSynonyms), daAntonyms));
      }
    }

    return result;
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
  // Examples that duplicate an example already on the card (a definition example from
  // dictionaryapi or google translate, or the book example sentence) are skipped. Comparison is
  // done on normalized text (lowercase, no punctuation, single spaces), the list is capped
  // because google translate returns every example it has and the card becomes overloaded.
  private List<String> mergeExamples(
      GoogleTranslation googleTranslation, List<Word> words, String myExample) {

    Set<String> cardExamples =
        Stream.concat(
                words.stream()
                    .flatMap(w -> w.getMeanings().stream())
                    .flatMap(m -> m.getDefinitions().stream())
                    .map(ru.dankoy.korvotoanki.core.domain.dictionaryapi.Definition::getExample),
                googleTranslation.getDefinitions().stream()
                    .map(ru.dankoy.korvotoanki.core.domain.googletranslation.Definition::example))
            .filter(Objects::nonNull)
            .map(this::normalizeExample)
            .collect(Collectors.toSet());

    if (Objects.nonNull(myExample)) {
      cardExamples.add(normalizeExample(myExample));
    }

    return googleTranslation.getExamples().stream()
        .filter(Objects::nonNull)
        .filter(example -> !cardExamples.contains(normalizeExample(example)))
        .limit(MAX_EXAMPLES)
        .toList();
  }

  // lowercase, punctuation removed, whitespace collapsed to single spaces
  private String normalizeExample(String example) {

    return example.toLowerCase().replaceAll("\\p{P}+", "").replaceAll("\\s+", " ").trim();
  }
}
