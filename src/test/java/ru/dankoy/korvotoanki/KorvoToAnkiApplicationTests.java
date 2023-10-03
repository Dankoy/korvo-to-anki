package ru.dankoy.korvotoanki;


import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import ru.dankoy.korvotoanki.config.appprops.DebugProperties;
import ru.dankoy.korvotoanki.config.appprops.DictionaryApiProperties;
import ru.dankoy.korvotoanki.config.appprops.ExternalApiProperties;
import ru.dankoy.korvotoanki.config.appprops.FilesProperties;
import ru.dankoy.korvotoanki.config.appprops.GoogleTranslatorProperties;
import ru.dankoy.korvotoanki.core.command.AnkiConverterCommand;
import ru.dankoy.korvotoanki.core.command.AnkiExporterCommand;
import ru.dankoy.korvotoanki.core.command.DictionaryApiCommand;
import ru.dankoy.korvotoanki.core.command.GoogleTranslateCommand;
import ru.dankoy.korvotoanki.core.command.TitleCommand;
import ru.dankoy.korvotoanki.core.command.VocabularyCommand;
import ru.dankoy.korvotoanki.core.dao.title.TitleDaoJdbc;
import ru.dankoy.korvotoanki.core.dao.vocabulary.VocabularyDaoJdbc;
import ru.dankoy.korvotoanki.core.fabric.anki.AnkiDataFabricImpl;
import ru.dankoy.korvotoanki.core.service.converter.AnkiConverterServiceImpl;
import ru.dankoy.korvotoanki.core.service.dictionaryapi.DictionaryServiceOkHttp;
import ru.dankoy.korvotoanki.core.service.exporter.ExporterServiceAnkiAsync;
import ru.dankoy.korvotoanki.core.service.filenameformatter.FileNameFormatterServiceImpl;
import ru.dankoy.korvotoanki.core.service.fileprovider.FileProviderServiceImpl;
import ru.dankoy.korvotoanki.core.service.googletrans.GoogleTranslatorOkHttp;
import ru.dankoy.korvotoanki.core.service.googletrans.parser.GoogleTranslatorParserImpl;
import ru.dankoy.korvotoanki.core.service.objectmapper.ObjectMapperServiceImpl;
import ru.dankoy.korvotoanki.core.service.state.StateServiceImpl;
import ru.dankoy.korvotoanki.core.service.templatebuilder.TemplateBuilder;
import ru.dankoy.korvotoanki.core.service.templatecreator.TemplateCreatorServiceImpl;
import ru.dankoy.korvotoanki.core.service.title.TitleServiceJdbc;
import ru.dankoy.korvotoanki.core.service.vocabulary.VocabularyServiceJdbc;

@DisplayName("Test default context ")
@SpringBootTest
class KorvoToAnkiApplicationTests {

  @Autowired
  ApplicationContext context;

  @DisplayName("all default necessary beans should be created")
  @Test
  void contextLoads() {

    var okHttpClient = context.getBean(OkHttpClient.class);
    var debugProperties = context.getBean(DebugProperties.class);
    var templateBuilder = context.getBean(TemplateBuilder.class);
    var cacheManager = context.getBean(CacheManager.class);
    var objectMapper = context.getBean(ObjectMapper.class);
    var googleTranslatorProperties = context.getBean(GoogleTranslatorProperties.class);
    var dictionaryApiProperties = context.getBean(DictionaryApiProperties.class);
    var externalApiProperties = context.getBean(ExternalApiProperties.class);
    var filesProperties = context.getBean(FilesProperties.class);
    var ankiConverterCommand = context.getBean(AnkiConverterCommand.class);
    var ankiExporterCommand = context.getBean(AnkiExporterCommand.class);
    var dictionaryApiCommand = context.getBean(DictionaryApiCommand.class);
    var googleTranslateCommand = context.getBean(GoogleTranslateCommand.class);
    var titleCommand = context.getBean(TitleCommand.class);
    var vocabularyCommand = context.getBean(VocabularyCommand.class);
    var titleDaoJdbc = context.getBean(TitleDaoJdbc.class);
    var vocabularyDaoJdbc = context.getBean(VocabularyDaoJdbc.class);
    var ankiDataFabric = context.getBean(AnkiDataFabricImpl.class);
    var ankiConverterService = context.getBean(AnkiConverterServiceImpl.class);
    var dictionaryServiceOkHttp = context.getBean(DictionaryServiceOkHttp.class);
    var exporterServiceAnkiAsync = context.getBean(ExporterServiceAnkiAsync.class);
//    var exporterServiceAnki = context.getBean(ExporterServiceAnki.class);
    var fileNameFormatterService = context.getBean(FileNameFormatterServiceImpl.class);
    var fileProviderService = context.getBean(FileProviderServiceImpl.class);
    var googleTranslatorOkHttp = context.getBean(GoogleTranslatorOkHttp.class);
    var googleTranslatorParser = context.getBean(GoogleTranslatorParserImpl.class);
    var objectMapperService = context.getBean(ObjectMapperServiceImpl.class);
    var stateService = context.getBean(StateServiceImpl.class);
    var templateCreatorService = context.getBean(TemplateCreatorServiceImpl.class);
    var titleServiceJdbc = context.getBean(TitleServiceJdbc.class);
    var vocabularyServiceJdbc = context.getBean(VocabularyServiceJdbc.class);

    assertNotNull(okHttpClient);
    assertNotNull(debugProperties);
    assertNotNull(templateBuilder);
    assertNotNull(cacheManager);
    assertNotNull(googleTranslatorProperties);
    assertNotNull(dictionaryApiProperties);
    assertNotNull(objectMapper);
    assertNotNull(externalApiProperties);
    assertNotNull(filesProperties);
    assertNotNull(ankiConverterCommand);
    assertNotNull(objectMapperService);
    assertNotNull(ankiExporterCommand);
    assertNotNull(dictionaryApiCommand);
    assertNotNull(googleTranslateCommand);
    assertNotNull(titleCommand);
    assertNotNull(vocabularyCommand);
    assertNotNull(titleDaoJdbc);
    assertNotNull(vocabularyDaoJdbc);
    assertNotNull(ankiDataFabric);
    assertNotNull(ankiConverterService);
    assertNotNull(dictionaryServiceOkHttp);
    assertNotNull(exporterServiceAnkiAsync);
    assertNotNull(fileNameFormatterService);
    assertNotNull(fileProviderService);
    assertNotNull(googleTranslatorOkHttp);
    assertNotNull(googleTranslatorParser);
    assertNotNull(stateService);
    assertNotNull(templateCreatorService);
    assertNotNull(titleServiceJdbc);
    assertNotNull(vocabularyServiceJdbc);

  }

}
