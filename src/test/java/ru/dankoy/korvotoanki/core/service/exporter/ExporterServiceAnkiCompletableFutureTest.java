package ru.dankoy.korvotoanki.core.service.exporter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.dankoy.korvotoanki.config.appprops.FilesParams;
import ru.dankoy.korvotoanki.config.appprops.FilesProperties;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;
import ru.dankoy.korvotoanki.core.domain.anki.Meaning;
import ru.dankoy.korvotoanki.core.domain.googletranslation.Definition;
import ru.dankoy.korvotoanki.core.domain.googletranslation.GoogleTranslation;
import ru.dankoy.korvotoanki.core.service.converter.AnkiConverterService;
import ru.dankoy.korvotoanki.core.service.converter.AnkiConverterServiceCompletableFuture;
import ru.dankoy.korvotoanki.core.service.datetimeprovider.DateTimeProviderImpl;
import ru.dankoy.korvotoanki.core.service.filenameformatter.FileNameFormatterService;
import ru.dankoy.korvotoanki.core.service.filenameformatter.FileNameFormatterServiceImpl;
import ru.dankoy.korvotoanki.core.service.fileprovider.FileProviderService;
import ru.dankoy.korvotoanki.core.service.fileprovider.FileProviderServiceImpl;
import ru.dankoy.korvotoanki.core.service.io.IOService;
import ru.dankoy.korvotoanki.core.service.io.IOServiceFile;
import ru.dankoy.korvotoanki.core.service.state.StateService;
import ru.dankoy.korvotoanki.core.service.state.StateServiceImpl;
import ru.dankoy.korvotoanki.core.service.templatecreator.TemplateCreatorService;
import ru.dankoy.korvotoanki.core.service.templatecreator.TemplateCreatorServiceImpl;
import ru.dankoy.korvotoanki.core.service.vocabulary.VocabularyService;
import ru.dankoy.korvotoanki.core.service.vocabulary.VocabularyServiceJdbc;

@DisplayName("Test ExporterServiceAnkiCompletableFutureTest ")
@SpringBootTest(
    classes = {
      VocabularyServiceJdbc.class,
      AnkiConverterServiceCompletableFuture.class,
      TemplateCreatorServiceImpl.class,
      FilesParams.class,
      StateServiceImpl.class,
      IOServiceFile.class,
      ExporterServiceAnkiCompletableFuture.class,
      FileProviderServiceImpl.class,
      FileNameFormatterServiceImpl.class,
      DateTimeProviderImpl.class
    },
    properties = {"korvo-to-anki.async-type=completable_future", "korvo-to-anki.async=true"})
@ExtendWith(MockitoExtension.class)
class ExporterServiceAnkiCompletableFutureTest {

  @MockitoBean private VocabularyService vocabularyService;

  @MockitoBean private AnkiConverterService ankiConverterService;

  @MockitoBean private TemplateCreatorService templateCreatorService;

  @MockitoBean private FilesProperties filesProperties;

  @Mock private IOService ioService; // prototype

  @MockitoBean private StateService stateService;

  @MockitoBean private FileProviderService fileProviderService;

  @MockitoBean private FileNameFormatterService fileNameFormatterServiceImpl;

  @InjectMocks @Autowired private ExporterServiceAnkiCompletableFuture exporterServiceAnkiAsync;

  @MockitoBean private Function<String, IOService> ioServiceFileFactory;

  @DisplayName("export one word without state")
  @Test
  void exportOneWordWithoutState() {

    var templ = "templ";
    List<Vocabulary> vocabularies = correctVocabularies();
    var sourceLanguage = "en";
    var targetLanguage = "ru";
    List<String> options = Stream.of("t").toList();

    var ankiData = getOneFromGoogleTranslate(vocabularies.get(0), getOneGt());

    given(ankiConverterService.convert(any(), eq(sourceLanguage), eq(targetLanguage), eq(options)))
        .willReturn(ankiData);
    given(vocabularyService.getAll()).willReturn(vocabularies);
    given(stateService.filterState(vocabularies)).willReturn(vocabularies);
    given(templateCreatorService.create(any())).willReturn(templ);
    doNothing().when(ioService).print(any());
    doNothing().when(stateService).saveState(vocabularies);
    given(ioServiceFileFactory.apply(anyString())).willReturn(ioService);
    given(filesProperties.getExportFileName()).willReturn("name");

    exporterServiceAnkiAsync.export(sourceLanguage, targetLanguage, options);

    Mockito.verify(vocabularyService, times(1)).getAll();
    Mockito.verify(stateService, times(1)).filterState(vocabularies);
    Mockito.verify(stateService, times(1)).saveState(vocabularies);
    Mockito.verify(templateCreatorService, times(1)).create(any());
    Mockito.verify(ioService, times(1)).print(any());
  }

  @DisplayName("export one word with state")
  @Test
  void exportOneWordWithState() {

    var templ = "templ";
    List<Vocabulary> vocabularies = correctVocabularies();
    var sourceLanguage = "en";
    var targetLanguage = "ru";
    List<String> options = Stream.of("t").toList();

    var ankiData = getOneFromGoogleTranslate(vocabularies.get(0), getOneGt());

    given(ankiConverterService.convert(any(), eq(sourceLanguage), eq(targetLanguage), eq(options)))
        .willReturn(ankiData);
    given(vocabularyService.getAll()).willReturn(vocabularies);
    given(stateService.filterState(vocabularies)).willReturn(Collections.emptyList());
    given(templateCreatorService.create(any())).willReturn(templ);
    doNothing().when(stateService).saveState(vocabularies);

    exporterServiceAnkiAsync.export(sourceLanguage, targetLanguage, options);

    Mockito.verify(vocabularyService, times(1)).getAll();
    Mockito.verify(stateService, times(1)).filterState(vocabularies);
    Mockito.verify(stateService, times(0)).saveState(vocabularies);
    Mockito.verify(templateCreatorService, times(0)).create(any());
    Mockito.verify(ioService, times(0)).print(any());
  }

  @DisplayName("export many words with state.")
  @Test
  void exportManyWordsWithState() {

    var templ = "templ";
    List<Vocabulary> vocabularies =
        Stream.generate(this::correctVocabularies).limit(10).flatMap(Collection::stream).toList();
    var sourceLanguage = "en";
    var targetLanguage = "ru";
    List<String> options = Stream.of("t").toList();

    var ankiData = getOneFromGoogleTranslate(vocabularies.get(0), getOneGt());

    given(ankiConverterService.convert(any(), eq(sourceLanguage), eq(targetLanguage), eq(options)))
        .willReturn(ankiData);
    given(vocabularyService.getAll()).willReturn(vocabularies);
    given(stateService.filterState(vocabularies)).willReturn(Collections.emptyList());
    given(templateCreatorService.create(any())).willReturn(templ);
    doNothing().when(stateService).saveState(vocabularies);

    exporterServiceAnkiAsync.export(sourceLanguage, targetLanguage, options);

    Mockito.verify(vocabularyService, times(1)).getAll();
    Mockito.verify(stateService, times(1)).filterState(vocabularies);
    Mockito.verify(stateService, times(0)).saveState(vocabularies);
    Mockito.verify(templateCreatorService, times(0)).create(any());
    Mockito.verify(ioService, times(0)).print(any());
  }

  @DisplayName("export many words without state. Should split list in two halves.")
  @Test
  void exportManyWordsWithoutState() {

    var templ = "templ";
    List<Vocabulary> vocabularies =
        Stream.generate(this::correctVocabularies).limit(10).flatMap(Collection::stream).toList();
    var sourceLanguage = "en";
    var targetLanguage = "ru";
    List<String> options = Stream.of("t").toList();

    var ankiData = getOneFromGoogleTranslate(vocabularies.get(0), getOneGt());

    given(ankiConverterService.convert(any(), eq(sourceLanguage), eq(targetLanguage), eq(options)))
        .willReturn(ankiData);
    given(vocabularyService.getAll()).willReturn(vocabularies);
    given(stateService.filterState(vocabularies)).willReturn(vocabularies);
    given(templateCreatorService.create(any())).willReturn(templ);
    doNothing().when(ioService).print(any());
    doNothing().when(stateService).saveState(vocabularies);
    given(ioServiceFileFactory.apply(anyString())).willReturn(ioService);
    given(filesProperties.getExportFileName()).willReturn("name");

    exporterServiceAnkiAsync.export(sourceLanguage, targetLanguage, options);

    Mockito.verify(vocabularyService, times(1)).getAll();
    Mockito.verify(stateService, times(1)).filterState(vocabularies);
    Mockito.verify(stateService, times(1)).saveState(vocabularies);
    Mockito.verify(templateCreatorService, times(1)).create(any());
    Mockito.verify(ioService, times(1)).print(any());
  }

  private List<Vocabulary> correctVocabularies() {

    var title = new Title(1L, "Title1", 1L);

    return Stream.of(
            new Vocabulary("word", title, 1695239837, 1695239837, 1695240137, 0, null, null, 0))
        .toList();
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
}
