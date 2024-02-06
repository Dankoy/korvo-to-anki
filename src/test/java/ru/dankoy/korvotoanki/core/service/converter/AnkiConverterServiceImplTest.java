package ru.dankoy.korvotoanki.core.service.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.dankoy.korvotoanki.config.appprops.ExternalApiParams;
import ru.dankoy.korvotoanki.config.appprops.ExternalApiProperties;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;
import ru.dankoy.korvotoanki.core.domain.anki.Meaning;
import ru.dankoy.korvotoanki.core.domain.dictionaryapi.Phonetics;
import ru.dankoy.korvotoanki.core.domain.dictionaryapi.Word;
import ru.dankoy.korvotoanki.core.domain.googletranslation.Definition;
import ru.dankoy.korvotoanki.core.domain.googletranslation.GoogleTranslation;
import ru.dankoy.korvotoanki.core.fabric.anki.AnkiDataFabric;
import ru.dankoy.korvotoanki.core.fabric.anki.AnkiDataFabricImpl;
import ru.dankoy.korvotoanki.core.service.dictionaryapi.DictionaryService;
import ru.dankoy.korvotoanki.core.service.dictionaryapi.DictionaryServiceOkHttp;
import ru.dankoy.korvotoanki.core.service.googletrans.GoogleTranslator;
import ru.dankoy.korvotoanki.core.service.googletrans.GoogleTranslatorOkHttp;

@DisplayName("Test AnkiConverterServiceImpl ")
@SpringBootTest(
    classes = {
      AnkiConverterServiceImpl.class,
      DictionaryServiceOkHttp.class,
      GoogleTranslatorOkHttp.class,
      AnkiDataFabricImpl.class,
      ExternalApiParams.class
    })
class AnkiConverterServiceImplTest {

  @MockBean private DictionaryService dictionaryService;

  @MockBean private GoogleTranslator googleTranslator;

  @MockBean private AnkiDataFabric ankiDataFabric;

  @MockBean private ExternalApiProperties externalApiProperties;

  @Autowired private AnkiConverterService ankiConverterService;

  @DisplayName("convert with non english source and dictionary api enabled")
  @Test
  void convertNonEnglishSourceAndDictionaryApiEnabled() {

    var sourceLanguage = "ja";
    var targetLanguage = "whatever";
    List<String> options = Stream.of("t", "md", "at", "rm").toList();

    List<Word> daResult = getWords(true);
    var vocabulary = getOneVocab();
    var gtResult = getOneGt();
    var ankiData = getOneFromGoogleTranslate(vocabulary, gtResult);

    given(externalApiProperties.isDictionaryApiEnabled()).willReturn(true);

    given(googleTranslator.translate(vocabulary.word(), targetLanguage, sourceLanguage, options))
        .willReturn(gtResult);

    given(ankiDataFabric.createAnkiData(vocabulary, gtResult, daResult)).willReturn(ankiData);

    var actual = ankiConverterService.convert(vocabulary, sourceLanguage, targetLanguage, options);

    assertThat(actual).isEqualTo(ankiData);

    Mockito.verify(ankiDataFabric, times(1)).createAnkiData(vocabulary, gtResult, daResult);
    Mockito.verify(googleTranslator, times(1))
        .translate(vocabulary.word(), targetLanguage, sourceLanguage, options);
    Mockito.verify(dictionaryService, times(0)).define(vocabulary.word());
    Mockito.verify(externalApiProperties, times(1)).isDictionaryApiEnabled();
  }

  @DisplayName("convert with non english source and dictionary api disabled")
  @Test
  void convertNonEnglishSourceAndDictionaryApiDisabled() {

    var sourceLanguage = "ja";
    var targetLanguage = "whatever";
    List<String> options = Stream.of("t", "md", "at", "rm").toList();

    List<Word> daResult = getWords(true);
    var vocabulary = getOneVocab();
    var gtResult = getOneGt();
    var ankiData = getOneFromGoogleTranslate(vocabulary, gtResult);

    given(externalApiProperties.isDictionaryApiEnabled()).willReturn(false);
    given(googleTranslator.translate(vocabulary.word(), targetLanguage, sourceLanguage, options))
        .willReturn(gtResult);
    given(ankiDataFabric.createAnkiData(vocabulary, gtResult, daResult)).willReturn(ankiData);

    var actual = ankiConverterService.convert(vocabulary, sourceLanguage, targetLanguage, options);

    assertThat(actual).isEqualTo(ankiData);

    Mockito.verify(ankiDataFabric, times(1)).createAnkiData(vocabulary, gtResult, daResult);
    Mockito.verify(googleTranslator, times(1))
        .translate(vocabulary.word(), targetLanguage, sourceLanguage, options);
    Mockito.verify(dictionaryService, times(0)).define(vocabulary.word());
    Mockito.verify(externalApiProperties, times(1)).isDictionaryApiEnabled();
  }

  @DisplayName("convert with english source and dictionary api disabled")
  @Test
  void convertEnglishSourceAndDictionaryApiDisabled() {

    var sourceLanguage = "en";
    var targetLanguage = "whatever";
    List<String> options = Stream.of("t", "md", "at", "rm").toList();

    List<Word> daResult = getWords(true);
    var vocabulary = getOneVocab();
    var gtResult = getOneGt();
    var ankiData = getOneFromGoogleTranslate(vocabulary, gtResult);

    given(externalApiProperties.isDictionaryApiEnabled()).willReturn(false);
    given(googleTranslator.translate(vocabulary.word(), targetLanguage, sourceLanguage, options))
        .willReturn(gtResult);
    given(ankiDataFabric.createAnkiData(vocabulary, gtResult, daResult)).willReturn(ankiData);

    var actual = ankiConverterService.convert(vocabulary, sourceLanguage, targetLanguage, options);

    assertThat(actual).isEqualTo(ankiData);

    Mockito.verify(ankiDataFabric, times(1)).createAnkiData(vocabulary, gtResult, daResult);
    Mockito.verify(googleTranslator, times(1))
        .translate(vocabulary.word(), targetLanguage, sourceLanguage, options);
    Mockito.verify(dictionaryService, times(0)).define(vocabulary.word());
    Mockito.verify(externalApiProperties, times(1)).isDictionaryApiEnabled();
  }

  @DisplayName("convert with english source and dictionary api enabled")
  @Test
  void convertEnglishSourceAndDictionaryApiEnabled() {

    var sourceLanguage = "en";
    var targetLanguage = "whatever";
    List<String> options = Stream.of("t", "md", "at", "rm").toList();

    List<Word> daResult = getWords(false);
    var vocabulary = getOneVocab();
    var gtResult = getOneGt();
    var ankiData = getOneFromGoogleTranslate(vocabulary, gtResult);

    given(externalApiProperties.isDictionaryApiEnabled()).willReturn(true);
    given(googleTranslator.translate(vocabulary.word(), targetLanguage, sourceLanguage, options))
        .willReturn(gtResult);
    given(ankiDataFabric.createAnkiData(vocabulary, gtResult, daResult)).willReturn(ankiData);
    given(dictionaryService.define(vocabulary.word())).willReturn(daResult);

    var actual = ankiConverterService.convert(vocabulary, sourceLanguage, targetLanguage, options);

    assertThat(actual).isEqualTo(ankiData);

    Mockito.verify(ankiDataFabric, times(1)).createAnkiData(vocabulary, gtResult, daResult);
    Mockito.verify(googleTranslator, times(1))
        .translate(vocabulary.word(), targetLanguage, sourceLanguage, options);
    Mockito.verify(dictionaryService, times(1)).define(vocabulary.word());
    Mockito.verify(externalApiProperties, times(1)).isDictionaryApiEnabled();
  }

  private AnkiData getOneFromGoogleTranslate(Vocabulary vocabulary, GoogleTranslation gtResult) {

    return AnkiData.builder()
        .book(vocabulary.title().name())
        .translations(gtResult.getTranslations())
        .meanings(
            gtResult.getDefinitions().stream()
                .map(
                    gtd ->
                        new Meaning(
                            gtd.type(),
                            Collections.singletonList(
                                new ru.dankoy.korvotoanki.core.domain.anki.Definition(
                                    gtd.info(), null)),
                            new ArrayList<>(),
                            new ArrayList<>()))
                .toList())
        .myExample(vocabulary.prevContext() + vocabulary.word() + vocabulary.nextContext())
        .transcription(gtResult.getTranscription())
        .build();
  }

  private GoogleTranslation getOneGt() {

    var gtResult = new GoogleTranslation("transcription");
    gtResult.getDefinitions().add(new Definition("type", "info"));
    gtResult.getTranslations().add("tr1");

    return gtResult;
  }

  private Vocabulary getOneVocab() {

    return new Vocabulary(
        "word", new Title(1L, "title1", 1L), 1L, 1L, 1L, 1L, "prev_context", "next_context", 0L);
  }

  private List<Word> getWords(boolean isEmpty) {

    if (isEmpty) {
      return Collections.singletonList(Word.emptyWord());
    } else {

      return Stream.of(
              new Word(
                  "data",
                  "phonetic",
                  Stream.of(new Phonetics("text", "audio", "source")).toList(),
                  Stream.of(
                          new ru.dankoy.korvotoanki.core.domain.dictionaryapi.Meaning(
                              "ps",
                              Stream.of(
                                      new ru.dankoy.korvotoanki.core.domain.dictionaryapi
                                          .Definition(
                                          "info",
                                          Stream.of("synonym1").toList(),
                                          Stream.of("antonym1").toList(),
                                          "example"))
                                  .toList(),
                              Stream.of("synonym1").toList(),
                              Stream.of("antonym1").toList()))
                      .toList()))
          .toList();
    }
  }
}
