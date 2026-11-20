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

    return AnkiData.builder()
        .word(vocabulary.word())
        .book(vocabulary.title().name())
        .translations(googleTranslation.getTranslations()) // could be empty list
        .transcription(chooseTranscription(googleTranslation, words))
        .myExample(obtainVocabularyExample(vocabulary))
        .meanings(mergeMeanings(googleTranslation, words))
        .examples(mergeExamples(googleTranslation, words))
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

  // dictionaryapi meanings are the base. Google translation meanings of parts of speech that
  // dictionaryapi doesn't have are added on top. Google translation synonyms are bound to
  // definitions by google translate internal definition ids, which dictionaryapi doesn't share,
  // so for dictionaryapi meanings the only merge level is the meaning itself: google translation
  // synonyms of the same part of speech are used only when the dictionaryapi meaning has none.
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

      List<String> synonyms = dictionaryApiMeaning.synonyms();
      if ((Objects.isNull(synonyms) || synonyms.isEmpty())
          && Objects.nonNull(googleMeaning)
          && Objects.nonNull(googleMeaning.synonyms())
          && !googleMeaning.synonyms().isEmpty()) {
        synonyms = googleMeaning.synonyms();
      }

      result.add(
          new Meaning(
              dictionaryApiMeaning.type(),
              dictionaryApiMeaning.definitions(),
              synonyms,
              dictionaryApiMeaning.antonyms()));
    }

    // parts of speech that only google translation has
    googleMeanings.stream()
        .filter(googleMeaning -> !dictionaryApiTypes.contains(googleMeaning.type()))
        .forEach(result::add);

    return result;
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
  // Examples that duplicate an example of a definition already on the card (from dictionaryapi
  // or from google translate itself) are skipped, the list is capped because google translate
  // returns every example it has and the card becomes overloaded.
  private List<String> mergeExamples(GoogleTranslation googleTranslation, List<Word> words) {

    Set<String> definitionExamples =
        Stream.concat(
                words.stream()
                    .flatMap(w -> w.getMeanings().stream())
                    .flatMap(m -> m.getDefinitions().stream())
                    .map(ru.dankoy.korvotoanki.core.domain.dictionaryapi.Definition::getExample),
                googleTranslation.getDefinitions().stream()
                    .map(ru.dankoy.korvotoanki.core.domain.googletranslation.Definition::example))
            .filter(Objects::nonNull)
            .map(String::trim)
            .collect(Collectors.toSet());

    return googleTranslation.getExamples().stream()
        .filter(example -> !definitionExamples.contains(example.trim()))
        .limit(MAX_EXAMPLES)
        .toList();
  }
}
