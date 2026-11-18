package ru.dankoy.korvotoanki.core.fabric.anki;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.domain.anki.Definition;
import ru.dankoy.korvotoanki.core.domain.anki.Meaning;
import ru.dankoy.korvotoanki.core.domain.dictionaryapi.Phonetics;
import ru.dankoy.korvotoanki.core.domain.dictionaryapi.Word;
import ru.dankoy.korvotoanki.core.domain.googletranslation.GoogleTranslation;
import ru.dankoy.korvotoanki.core.domain.googletranslation.Synonym;

@DisplayName("Test AnkiDataFabricImpl ")
class AnkiDataFabricImplTest {

  private final AnkiDataFabricImpl fabric = new AnkiDataFabricImpl();

  @DisplayName("create anki data with dictionaryapi data takes precedence over google translate")
  @Test
  void createAnkiDataWithDictionaryApiData() {

    var vocabulary = getOneVocab();
    var googleTranslation = getFullGoogleTranslation();

    List<Word> words =
        List.of(
            new Word(
                "word",
                "da_transcription",
                List.of(new Phonetics("text", "audio", "source")),
                List.of(
                    new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Meaning(
                        "ps",
                        List.of(
                            new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Definition(
                                "da_info",
                                List.of("da_definition_synonym"),
                                List.of("da_definition_antonym"),
                                "da_example")),
                        List.of("da_synonym"),
                        List.of("da_antonym")))));

    var ankiData = fabric.createAnkiData(vocabulary, googleTranslation, words);

    assertThat(ankiData.getWord()).isEqualTo(vocabulary.word());
    assertThat(ankiData.getBook()).isEqualTo(vocabulary.title().name());
    assertThat(ankiData.getTranslations())
        .isEqualTo(googleTranslation.getTranslations()); // always from google translate
    assertThat(ankiData.getTranscription()).isEqualTo("da_transcription");
    assertThat(ankiData.getMyExample())
        .isEqualTo(vocabulary.prevContext() + vocabulary.word() + vocabulary.nextContext());
    // only dictionaryapi meanings are used
    assertThat(ankiData.getMeanings())
        .containsExactly(
            new Meaning(
                "ps",
                List.of(
                    new Definition(
                        "da_info",
                        "da_example",
                        List.of("da_definition_synonym"),
                        List.of("da_definition_antonym"))),
                List.of("da_synonym"),
                List.of("da_antonym")));
    // google translate examples are not used when dictionaryapi has data
    assertThat(ankiData.getExamples()).isEmpty();
  }

  @DisplayName(
      "create anki data without dictionaryapi data falls back to google translate with synonyms"
          + " bound to definitions")
  @Test
  void createAnkiDataWithoutDictionaryApiData() {

    var vocabulary = getOneVocab();
    var googleTranslation = getFullGoogleTranslation();

    var ankiData = fabric.createAnkiData(vocabulary, googleTranslation, List.of(Word.emptyWord()));

    assertThat(ankiData.getTranscription()).isEqualTo("gt_transcription");
    assertThat(ankiData.getMeanings())
        .containsExactlyInAnyOrder(
            new Meaning(
                "noun",
                List.of(
                    new Definition(
                        "a greeting or reception",
                        "the proposal got a warm welcome",
                        List.of("приветствие", "салют"),
                        List.of())),
                List.of(),
                List.of()),
            new Meaning(
                "exclamation",
                List.of(
                    new Definition(
                        "an expression of greeting",
                        "every morning they exchanged polite hellos",
                        List.of("greeting"),
                        List.of()),
                    new Definition(
                        "used to begin a phone conversation", null, List.of(), List.of())),
                List.of("здравствуй"),
                List.of()));
    assertThat(ankiData.getExamples())
        .containsExactly("La fourchette des prix.", "Another example sentence.");
  }

  @DisplayName("create anki data without any data results in empty meanings")
  @Test
  void createAnkiDataWithoutAnyData() {

    var vocabulary = getOneVocab();
    var googleTranslation = new GoogleTranslation("gt_transcription");
    googleTranslation.getTranslations().add("мелочь");

    var ankiData = fabric.createAnkiData(vocabulary, googleTranslation, List.of(Word.emptyWord()));

    assertThat(ankiData.getTranscription()).isEqualTo("gt_transcription");
    assertThat(ankiData.getTranslations()).containsExactly("мелочь");
    assertThat(ankiData.getMeanings()).isEmpty();
    assertThat(ankiData.getExamples()).isEmpty();
  }

  @DisplayName("transcription is taken from dictionaryapi phonetics when phonetic is null")
  @Test
  void createAnkiDataTranscriptionFromDictionaryApiPhonetics() {

    var vocabulary = getOneVocab();
    var googleTranslation = new GoogleTranslation("gt_transcription");

    List<Word> words =
        List.of(
            new Word(
                "word",
                null,
                List.of(new Phonetics("phonetics_text", "audio", "source")),
                List.of()));

    var ankiData = fabric.createAnkiData(vocabulary, googleTranslation, words);

    assertThat(ankiData.getTranscription()).isEqualTo("phonetics_text");
  }

  @DisplayName("google translate examples are capped at ten")
  @Test
  void createAnkiDataCapsGoogleTranslateExamples() {

    var vocabulary = getOneVocab();
    var googleTranslation = new GoogleTranslation("gt_transcription");
    for (int i = 1; i <= 15; i++) {
      googleTranslation.getExamples().add("example " + i);
    }

    var ankiData = fabric.createAnkiData(vocabulary, googleTranslation, List.of(Word.emptyWord()));

    assertThat(ankiData.getExamples()).hasSize(10);
    assertThat(ankiData.getExamples().getFirst()).isEqualTo("example 1");
    assertThat(ankiData.getExamples().getLast()).isEqualTo("example 10");
  }

  @DisplayName(
      "google translate synonyms without definitions (ss option without md) get meanings without"
          + " definitions")
  @Test
  void createAnkiDataSynonymsWithoutDefinitions() {

    var vocabulary = getOneVocab();
    var googleTranslation = new GoogleTranslation("gt_transcription");
    googleTranslation
        .getSynonyms()
        .addAll(
            List.of(
                new Synonym("noun", "noun.1", List.of("приветствие", "салют")),
                new Synonym("noun", null, List.of("здравствуй")),
                new Synonym("verb", null, List.of("звать"))));

    var ankiData = fabric.createAnkiData(vocabulary, googleTranslation, List.of(Word.emptyWord()));

    assertThat(ankiData.getMeanings())
        .containsExactlyInAnyOrder(
            new Meaning(
                "noun", List.of(), List.of("приветствие", "салют", "здравствуй"), List.of()),
            new Meaning("verb", List.of(), List.of("звать"), List.of()));
  }

  @DisplayName("synonyms are capped at fifteen on both levels")
  @Test
  void createAnkiDataCapsSynonyms() {

    var vocabulary = getOneVocab();
    var googleTranslation = new GoogleTranslation("gt_transcription");
    googleTranslation
        .getDefinitions()
        .add(
            new ru.dankoy.korvotoanki.core.domain.googletranslation.Definition(
                "noun", "a greeting", "hello there", "noun.1"));
    googleTranslation
        .getSynonyms()
        .addAll(
            List.of(
                new Synonym(
                    "noun",
                    "noun.1",
                    java.util.stream.IntStream.rangeClosed(1, 20)
                        .mapToObj(i -> "bound_synonym_" + i)
                        .toList()),
                new Synonym(
                    "noun",
                    null,
                    java.util.stream.IntStream.rangeClosed(1, 20)
                        .mapToObj(i -> "unbound_synonym_" + i)
                        .toList())));

    var ankiData = fabric.createAnkiData(vocabulary, googleTranslation, List.of(Word.emptyWord()));

    var noun =
        ankiData.getMeanings().stream()
            .filter(m -> m.type().equals("noun"))
            .findFirst()
            .orElseThrow();

    assertThat(noun.definitions().getFirst().synonyms()).hasSize(15);
    assertThat(noun.synonyms()).hasSize(15);
    assertThat(noun.definitions().getFirst().synonyms().getFirst()).isEqualTo("bound_synonym_1");
    assertThat(noun.synonyms().getFirst()).isEqualTo("unbound_synonym_1");
  }

  private GoogleTranslation getFullGoogleTranslation() {

    var googleTranslation = new GoogleTranslation("gt_transcription");
    googleTranslation.getTranslations().addAll(List.of("мелочь", "пустяк"));
    googleTranslation
        .getDefinitions()
        .addAll(
            List.of(
                new ru.dankoy.korvotoanki.core.domain.googletranslation.Definition(
                    "exclamation",
                    "an expression of greeting",
                    "every morning they exchanged polite hellos",
                    "exc.1"),
                new ru.dankoy.korvotoanki.core.domain.googletranslation.Definition(
                    "exclamation", "used to begin a phone conversation", null, null),
                new ru.dankoy.korvotoanki.core.domain.googletranslation.Definition(
                    "noun",
                    "a greeting or reception",
                    "the proposal got a warm welcome",
                    "noun.1")));
    googleTranslation
        .getSynonyms()
        .addAll(
            List.of(
                new Synonym("noun", "noun.1", List.of("приветствие", "салют")),
                new Synonym("exclamation", "exc.1", List.of("greeting")),
                new Synonym("exclamation", null, List.of("здравствуй"))));
    googleTranslation
        .getExamples()
        .addAll(List.of("La fourchette des prix.", "Another example sentence."));

    return googleTranslation;
  }

  private Vocabulary getOneVocab() {

    return new Vocabulary(
        "word", new Title(1L, "title1", 1L), 1L, 1L, 1L, 1L, "prev_context", "next_context", 0L);
  }
}
