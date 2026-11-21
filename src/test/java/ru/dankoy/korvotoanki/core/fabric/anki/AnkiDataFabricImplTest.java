package ru.dankoy.korvotoanki.core.fabric.anki;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
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

@DisplayName("Test AnkiDataFabricImpl ")
class AnkiDataFabricImplTest {

  private final AnkiDataFabricImpl fabric = new AnkiDataFabricImpl();

  @DisplayName(
      "create anki data with dictionaryapi data merges google translate examples and meanings"
          + " of missing parts of speech")
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

    var ankiData =
        fabric.createAnkiData(vocabulary, googleTranslation, words, FreeDictionaryWord.emptyWord());

    assertThat(ankiData.getWord()).isEqualTo(vocabulary.word());
    assertThat(ankiData.getBook()).isEqualTo(vocabulary.title().name());
    assertThat(ankiData.getTranslations())
        .isEqualTo(googleTranslation.getTranslations()); // always from google translate
    assertThat(ankiData.getTranscription()).isEqualTo("da_transcription");
    assertThat(ankiData.getMyExample())
        .isEqualTo(vocabulary.prevContext() + vocabulary.word() + vocabulary.nextContext());
    // dictionaryapi meaning stays, google translate meanings of other parts of speech are added
    assertThat(ankiData.getMeanings())
        .containsExactlyInAnyOrder(
            new Meaning(
                "ps",
                List.of(
                    new Definition(
                        "da_info",
                        "da_example",
                        List.of("da_definition_synonym"),
                        List.of("da_definition_antonym"))),
                List.of("da_synonym"),
                List.of("da_antonym")),
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
    // google translate examples are merged regardless of the dictionaryapi result
    assertThat(ankiData.getExamples())
        .containsExactly("La fourchette des prix.", "Another example sentence.");
  }

  @DisplayName(
      "create anki data merges google translate definitions and unions synonyms with dictionaryapi"
          + " meaning of the same part of speech")
  @Test
  void createAnkiDataMergesMeaningsOfSamePartOfSpeech() {

    var vocabulary = getOneVocab();
    var googleTranslation = getFullGoogleTranslation();
    // verb has no definitions in google translation data, so its synonyms are unbound
    googleTranslation.getSynonyms().add(new Synonym("verb", null, List.of("звать")));

    List<Word> words =
        List.of(
            new Word(
                "word",
                "da_transcription",
                List.of(new Phonetics("text", "audio", "source")),
                List.of(
                    new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Meaning(
                        "noun",
                        List.of(
                            new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Definition(
                                "da_noun_info",
                                List.of("da_noun_definition_synonym"),
                                List.of(),
                                "da_noun_example")),
                        List.of("da_noun_synonym"),
                        List.of()),
                    new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Meaning(
                        "verb",
                        List.of(
                            new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Definition(
                                "da_verb_info", List.of(), List.of(), "da_verb_example")),
                        List.of(),
                        List.of()))));

    var ankiData =
        fabric.createAnkiData(vocabulary, googleTranslation, words, FreeDictionaryWord.emptyWord());

    // no duplicated meanings for noun and exclamation of google translate
    assertThat(ankiData.getMeanings())
        .containsExactlyInAnyOrder(
            // dictionaryapi noun is the base, google translate definition of the same part of
            // speech is appended with its example and bound synonyms
            new Meaning(
                "noun",
                List.of(
                    new Definition(
                        "da_noun_info",
                        "da_noun_example",
                        List.of("da_noun_definition_synonym"),
                        List.of()),
                    new Definition(
                        "a greeting or reception",
                        "the proposal got a warm welcome",
                        List.of("приветствие", "салют"),
                        List.of())),
                List.of("da_noun_synonym"),
                List.of()),
            // dictionaryapi verb keeps its definitions, google translate verb synonyms are
            // unioned into the meaning level
            new Meaning(
                "verb",
                List.of(new Definition("da_verb_info", "da_verb_example", List.of(), List.of())),
                List.of("звать"),
                List.of()),
            // exclamation is missing in dictionaryapi and comes entirely from google translate
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
  }

  @DisplayName(
      "dictionaryapi word with several etymologies (multiple word entries) gets one meaning per"
          + " part of speech, definitions appended and exact duplicates merged")
  @Test
  void createAnkiDataGroupsMeaningsOfMultipleWordEntriesByPartOfSpeech() {

    var vocabulary = getOneVocab();
    var googleTranslation = new GoogleTranslation("gt_transcription");

    List<Word> words =
        List.of(
            new Word(
                "word",
                "da_transcription",
                List.of(),
                List.of(
                    new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Meaning(
                        "verb",
                        List.of(
                            new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Definition(
                                "v1_info", List.of("v1_synonym"), List.of(), "v1_example")),
                        List.of("verb_synonym_1"),
                        List.of()),
                    new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Meaning(
                        "adjective",
                        List.of(
                            new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Definition(
                                "adj_info", List.of(), List.of(), null)),
                        List.of(),
                        List.of()),
                    new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Meaning(
                        "adverb",
                        List.of(
                            new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Definition(
                                "adv1_info", List.of(), List.of(), null)),
                        List.of(),
                        List.of()))),
            new Word(
                "word",
                "da_transcription",
                List.of(),
                List.of(
                    new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Meaning(
                        "verb",
                        List.of(
                            // qualifier-tagged duplicate of v1 - merged, existing example and
                            // synonyms win
                            new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Definition(
                                "(rare, nonstandard) v1_info.",
                                List.of("v1_synonym_extra"),
                                List.of(),
                                "v1_example_new"),
                            new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Definition(
                                "v2_info", List.of(), List.of(), null)),
                        List.of("verb_synonym_2"),
                        List.of()),
                    new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Meaning(
                        "adverb",
                        List.of(
                            // case-insensitive duplicate of adv1 - merged
                            new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Definition(
                                "ADV1_info", List.of(), List.of(), null),
                            new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Definition(
                                "adv2_info", List.of(), List.of(), null)),
                        List.of("adverb_synonym"),
                        List.of()))));

    var ankiData =
        fabric.createAnkiData(vocabulary, googleTranslation, words, FreeDictionaryWord.emptyWord());

    assertThat(ankiData.getMeanings())
        .containsExactly(
            new Meaning(
                "verb",
                List.of(
                    new Definition(
                        "v1_info",
                        "v1_example",
                        List.of("v1_synonym", "v1_synonym_extra"),
                        List.of()),
                    new Definition("v2_info", null, List.of(), List.of())),
                List.of("verb_synonym_1", "verb_synonym_2"),
                List.of()),
            new Meaning(
                "adjective",
                List.of(new Definition("adj_info", null, List.of(), List.of())),
                List.of(),
                List.of()),
            new Meaning(
                "adverb",
                List.of(
                    new Definition("adv1_info", null, List.of(), List.of()),
                    new Definition("adv2_info", null, List.of(), List.of())),
                List.of("adverb_synonym"),
                List.of()));
  }

  @DisplayName("meaning level synonyms are union of dictionaryapi and google translate ones")
  @Test
  void createAnkiDataUnionsMeaningSynonyms() {

    var vocabulary = getOneVocab();
    var googleTranslation = new GoogleTranslation("gt_transcription");
    googleTranslation
        .getDefinitions()
        .add(
            new ru.dankoy.korvotoanki.core.domain.googletranslation.Definition(
                "noun", "gt_noun_info", null, null));
    googleTranslation.getSynonyms().add(new Synonym("noun", null, List.of("gt_synonym")));

    List<Word> words =
        List.of(
            new Word(
                "word",
                "da_transcription",
                List.of(new Phonetics("text", "audio", "source")),
                List.of(
                    new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Meaning(
                        "noun",
                        List.of(
                            new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Definition(
                                "da_noun_info", List.of(), List.of(), null)),
                        List.of("da_synonym"),
                        List.of()))));

    var ankiData =
        fabric.createAnkiData(vocabulary, googleTranslation, words, FreeDictionaryWord.emptyWord());

    var noun =
        ankiData.getMeanings().stream()
            .filter(m -> m.type().equals("noun"))
            .findFirst()
            .orElseThrow();

    assertThat(noun.synonyms()).containsExactly("da_synonym", "gt_synonym");
  }

  @DisplayName(
      "google translate definition with the same info as dictionaryapi one is merged: synonyms"
          + " are unioned and example is used when dictionaryapi has none")
  @Test
  void createAnkiDataMergesDuplicateDefinitions() {

    var vocabulary = getOneVocab();
    var googleTranslation = new GoogleTranslation("gt_transcription");
    googleTranslation
        .getDefinitions()
        .add(
            new ru.dankoy.korvotoanki.core.domain.googletranslation.Definition(
                "noun", "same info", "gt_example", "noun.1"));
    googleTranslation
        .getSynonyms()
        .add(new Synonym("noun", "noun.1", List.of("gt_synonym", "da_synonym")));

    List<Word> words =
        List.of(
            new Word(
                "word",
                "da_transcription",
                List.of(new Phonetics("text", "audio", "source")),
                List.of(
                    new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Meaning(
                        "noun",
                        List.of(
                            new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Definition(
                                "same info", List.of("da_synonym"), List.of(), null)),
                        List.of(),
                        List.of()))));

    var ankiData =
        fabric.createAnkiData(vocabulary, googleTranslation, words, FreeDictionaryWord.emptyWord());

    var noun =
        ankiData.getMeanings().stream()
            .filter(m -> m.type().equals("noun"))
            .findFirst()
            .orElseThrow();

    // single definition: synonyms unioned (dictionaryapi first, no duplicates), example taken
    // from google translate
    assertThat(noun.definitions())
        .containsExactly(
            new Definition(
                "same info", "gt_example", List.of("da_synonym", "gt_synonym"), List.of()));
  }

  @DisplayName(
      "create anki data skips google translate examples that duplicate definition examples from"
          + " dictionaryapi or google translate itself")
  @Test
  void createAnkiDataSkipsDuplicatedExamples() {

    var vocabulary = getOneVocab();
    var googleTranslation = getFullGoogleTranslation();
    googleTranslation.getExamples().add(0, "da_example");
    googleTranslation.getExamples().add(0, "every morning they exchanged polite hellos");

    List<Word> words =
        List.of(
            new Word(
                "word",
                "da_transcription",
                List.of(new Phonetics("text", "audio", "source")),
                List.of(
                    new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Meaning(
                        "noun",
                        List.of(
                            new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Definition(
                                "da_info", List.of(), List.of(), "da_example")),
                        List.of(),
                        List.of()))));

    var ankiData =
        fabric.createAnkiData(vocabulary, googleTranslation, words, FreeDictionaryWord.emptyWord());

    assertThat(ankiData.getExamples())
        .containsExactly("La fourchette des prix.", "Another example sentence.");
  }

  @DisplayName(
      "google translate examples are de-duplicated by normalized text and against the book"
          + " example sentence")
  @Test
  void createAnkiDataNormalizesExampleDuplicates() {

    var vocabulary = getOneVocab();
    var googleTranslation = new GoogleTranslation("gt_transcription");
    googleTranslation
        .getExamples()
        .addAll(
            List.of(
                "hello there katie", // differs from the definition example only in case/punctuation
                "prev_contextWORDnext_context", // the book example sentence with different case
                "unique example"));

    List<Word> words =
        List.of(
            new Word(
                "word",
                "da_transcription",
                List.of(new Phonetics("text", "audio", "source")),
                List.of(
                    new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Meaning(
                        "noun",
                        List.of(
                            new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Definition(
                                "da_info", List.of(), List.of(), "Hello there, Katie!")),
                        List.of(),
                        List.of()))));

    var ankiData =
        fabric.createAnkiData(vocabulary, googleTranslation, words, FreeDictionaryWord.emptyWord());

    assertThat(ankiData.getExamples()).containsExactly("unique example");
  }

  @DisplayName(
      "create anki data without dictionaryapi data falls back to google translate with synonyms"
          + " bound to definitions")
  @Test
  void createAnkiDataWithoutDictionaryApiData() {

    var vocabulary = getOneVocab();
    var googleTranslation = getFullGoogleTranslation();

    var ankiData =
        fabric.createAnkiData(
            vocabulary,
            googleTranslation,
            List.of(Word.emptyWord()),
            FreeDictionaryWord.emptyWord());

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

    var ankiData =
        fabric.createAnkiData(
            vocabulary,
            googleTranslation,
            List.of(Word.emptyWord()),
            FreeDictionaryWord.emptyWord());

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

    var ankiData =
        fabric.createAnkiData(vocabulary, googleTranslation, words, FreeDictionaryWord.emptyWord());

    assertThat(ankiData.getTranscription()).isEqualTo("phonetics_text");
  }

  @DisplayName("google translate examples are capped at twenty")
  @Test
  void createAnkiDataCapsGoogleTranslateExamples() {

    var vocabulary = getOneVocab();
    var googleTranslation = new GoogleTranslation("gt_transcription");
    for (int i = 1; i <= 25; i++) {
      googleTranslation.getExamples().add("example " + i);
    }

    var ankiData =
        fabric.createAnkiData(
            vocabulary,
            googleTranslation,
            List.of(Word.emptyWord()),
            FreeDictionaryWord.emptyWord());

    assertThat(ankiData.getExamples()).hasSize(20);
    assertThat(ankiData.getExamples().getFirst()).isEqualTo("example 1");
    assertThat(ankiData.getExamples().getLast()).isEqualTo("example 20");
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

    var ankiData =
        fabric.createAnkiData(
            vocabulary,
            googleTranslation,
            List.of(Word.emptyWord()),
            FreeDictionaryWord.emptyWord());

    assertThat(ankiData.getMeanings())
        .containsExactlyInAnyOrder(
            new Meaning(
                "noun", List.of(), List.of("приветствие", "салют", "здравствуй"), List.of()),
            new Meaning("verb", List.of(), List.of("звать"), List.of()));
  }

  @DisplayName("synonyms are capped at twenty on both levels")
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
                    java.util.stream.IntStream.rangeClosed(1, 25)
                        .mapToObj(i -> "bound_synonym_" + i)
                        .toList()),
                new Synonym(
                    "noun",
                    null,
                    java.util.stream.IntStream.rangeClosed(1, 25)
                        .mapToObj(i -> "unbound_synonym_" + i)
                        .toList())));

    var ankiData =
        fabric.createAnkiData(
            vocabulary,
            googleTranslation,
            List.of(Word.emptyWord()),
            FreeDictionaryWord.emptyWord());

    var noun =
        ankiData.getMeanings().stream()
            .filter(m -> m.type().equals("noun"))
            .findFirst()
            .orElseThrow();

    assertThat(noun.definitions().getFirst().synonyms()).hasSize(20);
    assertThat(noun.synonyms()).hasSize(20);
    assertThat(noun.definitions().getFirst().synonyms().getFirst()).isEqualTo("bound_synonym_1");
    assertThat(noun.synonyms().getFirst()).isEqualTo("unbound_synonym_1");
  }

  @DisplayName(
      "freedictionaryapi transcription has priority over dictionaryapi and google translate")
  @Test
  void createAnkiDataFreeDictionaryTranscriptionPriority() {

    var vocabulary = getOneVocab();
    var googleTranslation = getFullGoogleTranslation();

    List<Word> words = List.of(new Word("word", "da_transcription", null, new ArrayList<>()));

    var freeDictionaryWord = getFreeDictionaryWord();

    var ankiData = fabric.createAnkiData(vocabulary, googleTranslation, words, freeDictionaryWord);

    assertThat(ankiData.getTranscription()).isEqualTo("/fd_transcription/");
  }

  @DisplayName(
      "freedictionaryapi senses are merged into meanings of the same part of speech and its"
          + " parts of speech are added on top")
  @Test
  void createAnkiDataMergesFreeDictionaryMeanings() {

    var vocabulary = getOneVocab();
    var googleTranslation = getFullGoogleTranslation();

    List<Word> words =
        List.of(
            new Word(
                "word",
                "da_transcription",
                null,
                List.of(
                    new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Meaning(
                        "noun",
                        List.of(
                            new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Definition(
                                "da_info", null, null, "da_example")),
                        List.of("da_synonym"),
                        List.of("da_antonym")))));

    var freeDictionaryWord = getFreeDictionaryWord();

    var ankiData = fabric.createAnkiData(vocabulary, googleTranslation, words, freeDictionaryWord);

    var noun =
        ankiData.getMeanings().stream()
            .filter(m -> m.type().equals("noun"))
            .findFirst()
            .orElseThrow();

    // dictionaryapi definition stays first, freedictionaryapi senses with flattened subsenses
    // are appended after google translate definitions
    assertThat(noun.definitions())
        .containsExactly(
            new Definition("da_info", "da_example", null, null),
            new Definition(
                "a greeting or reception",
                "the proposal got a warm welcome",
                List.of("приветствие", "салют"),
                List.of()),
            new Definition("fd_definition", "fd_example1", List.of("fd_sense_synonym"), null),
            new Definition("fd_subsense", null, null, null));

    // entry level synonyms are unioned into meaning level synonyms, dictionaryapi first
    assertThat(noun.synonyms()).containsExactly("da_synonym", "fd_entry_synonym");
    assertThat(noun.antonyms()).containsExactly("da_antonym", "fd_entry_antonym");

    // part of speech that only freedictionaryapi has is added on top
    assertThat(ankiData.getMeanings().stream().map(Meaning::type))
        .containsExactlyInAnyOrder("noun", "exclamation", "verb");
  }

  @DisplayName(
      "duplicate freedictionaryapi definitions are merged: synonyms are unioned and its example"
          + " is used when the existing definition has none")
  @Test
  void createAnkiDataMergesDuplicateFreeDictionaryDefinitions() {

    var vocabulary = getOneVocab();
    var googleTranslation = getFullGoogleTranslation();

    List<Word> words =
        List.of(
            new Word(
                "word",
                null,
                null,
                List.of(
                    new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Meaning(
                        "verb",
                        List.of(
                            new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Definition(
                                "fd_definition", List.of("da_synonym"), null, null)),
                        null,
                        null))));

    var freeDictionaryWord = getFreeDictionaryWord();

    var ankiData = fabric.createAnkiData(vocabulary, googleTranslation, words, freeDictionaryWord);

    var verb =
        ankiData.getMeanings().stream()
            .filter(m -> m.type().equals("verb"))
            .findFirst()
            .orElseThrow();

    assertThat(verb.definitions())
        .containsExactly(
            new Definition(
                "fd_definition", "fd_example1", List.of("da_synonym", "fd_sense_synonym"), null));
  }

  @DisplayName(
      "freedictionaryapi sense examples that are not definition examples go to the card examples"
          + " section")
  @Test
  void createAnkiDataMergesFreeDictionaryExamples() {

    var vocabulary = getOneVocab();
    var googleTranslation = getFullGoogleTranslation();

    List<Word> words = List.of(new Word("word", null, null, new ArrayList<>()));

    var freeDictionaryWord = getFreeDictionaryWord();

    var ankiData = fabric.createAnkiData(vocabulary, googleTranslation, words, freeDictionaryWord);

    // fd_example2 is the second example of a sense, fd_example1 became a definition example
    assertThat(ankiData.getExamples())
        .containsExactly("La fourchette des prix.", "Another example sentence.", "fd_example2");
  }

  @DisplayName("anki data is created from freedictionaryapi data only")
  @Test
  void createAnkiDataWithFreeDictionaryDataOnly() {

    var vocabulary = getOneVocab();
    var googleTranslation = getFullGoogleTranslation();

    var freeDictionaryWord = getFreeDictionaryWord();

    var ankiData =
        fabric.createAnkiData(
            vocabulary, googleTranslation, List.of(Word.emptyWord()), freeDictionaryWord);

    assertThat(ankiData.getTranscription()).isEqualTo("/fd_transcription/");
    // google translate meanings stay, freedictionaryapi parts of speech are merged into them or
    // added on top
    assertThat(ankiData.getMeanings().stream().map(Meaning::type))
        .containsExactlyInAnyOrder("noun", "exclamation", "verb");
  }

  @DisplayName(
      "wiktionary examples with quotation references are stripped to the sentence only and"
          + " whitespace is collapsed")
  @Test
  void createAnkiDataCleansFreeDictionaryExamples() {

    var vocabulary = getOneVocab();
    var googleTranslation = new GoogleTranslation("gt_transcription");

    List<Word> words = List.of(new Word("word", null, null, new ArrayList<>()));

    var freeDictionaryWord =
        new FreeDictionaryWord(
            "word",
            List.of(
                new FreeDictionaryEntry(
                    "noun",
                    List.of(new Pronunciation("ipa", "/fd_transcription/", List.of())),
                    List.of(
                        new FreeDictionarySense(
                            "fd_definition",
                            List.of(
                                "1850s, Cardinal John Henry Newman, The Rise and Progress of"
                                    + " Universities\nto do some substantial good, is the"
                                    + " compensation for much incidental imperfection",
                                "For making tea I have a kettle,\nBesides a pan made of run metal",
                                "second example\nwith newline"),
                            null,
                            null,
                            null)),
                    null,
                    null)));

    var ankiData = fabric.createAnkiData(vocabulary, googleTranslation, words, freeDictionaryWord);

    var noun =
        ankiData.getMeanings().stream()
            .filter(m -> m.type().equals("noun"))
            .findFirst()
            .orElseThrow();

    // quotation reference line is dropped, only the sentence is kept
    assertThat(noun.definitions().getFirst().example())
        .isEqualTo(
            "to do some substantial good, is the compensation for much incidental imperfection");
    // multi-line verse without a reference is kept whole, whitespace collapsed
    assertThat(ankiData.getExamples())
        .containsExactly(
            "For making tea I have a kettle, Besides a pan made of run metal",
            "second example with newline");
  }

  private FreeDictionaryWord getFreeDictionaryWord() {

    return new FreeDictionaryWord(
        "word",
        List.of(
            new FreeDictionaryEntry(
                "noun",
                List.of(new Pronunciation("ipa", "/fd_transcription/", List.of("UK"))),
                List.of(
                    new FreeDictionarySense(
                        "fd_definition",
                        List.of("fd_example1", "fd_example2"),
                        List.of("fd_sense_synonym"),
                        null,
                        List.of(new FreeDictionarySense("fd_subsense", null, null, null, null)))),
                List.of("fd_entry_synonym"),
                List.of("fd_entry_antonym")),
            new FreeDictionaryEntry(
                "verb",
                List.of(new Pronunciation("ipa", "/fd_transcription/", List.of("UK"))),
                List.of(
                    new FreeDictionarySense(
                        "fd_definition",
                        List.of("fd_example1", "fd_example2"),
                        List.of("fd_sense_synonym"),
                        null,
                        null)),
                null,
                null)));
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
