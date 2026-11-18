package ru.dankoy.korvotoanki;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.cache.CacheManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestPropertySource;
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
import ru.dankoy.korvotoanki.core.dao.vocabularybuilder.title.TitleDaoJdbc;
import ru.dankoy.korvotoanki.core.dao.vocabularybuilder.vocabulary.VocabularyDaoJdbc;
import ru.dankoy.korvotoanki.core.fabric.anki.AnkiDataFabricImpl;
import ru.dankoy.korvotoanki.core.service.converter.AnkiConverterServiceCompletableFuture;
import ru.dankoy.korvotoanki.core.service.dictionaryapi.DictionaryServiceWebClient;
import ru.dankoy.korvotoanki.core.service.exporter.ExporterServiceAnkiCompletableFuture;
import ru.dankoy.korvotoanki.core.service.filenameformatter.FileNameFormatterServiceImpl;
import ru.dankoy.korvotoanki.core.service.fileprovider.FileProviderServiceImpl;
import ru.dankoy.korvotoanki.core.service.googletrans.GoogleTranslatorWebClient;
import ru.dankoy.korvotoanki.core.service.googletrans.parser.GoogleTranslatorParserImpl;
import ru.dankoy.korvotoanki.core.service.objectmapper.ObjectMapperServiceImpl;
import ru.dankoy.korvotoanki.core.service.state.StateServiceImpl;
import ru.dankoy.korvotoanki.core.service.state.StateServiceSqlite;
import ru.dankoy.korvotoanki.core.service.templatebuilder.TemplateBuilder;
import ru.dankoy.korvotoanki.core.service.templatecreator.TemplateCreatorServiceImpl;
import ru.dankoy.korvotoanki.core.service.title.TitleServiceJdbc;
import ru.dankoy.korvotoanki.core.service.vocabulary.VocabularyServiceJdbc;
import tools.jackson.databind.json.JsonMapper;

@DisplayName("Test default context ")
@TestPropertySource(
    properties = {
      "spring.autoconfigure.exclude=org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration"
    })
@SpringBootTest
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE) // use embedded database to run migrations on
class KorvoToAnkiApplicationTests {

  @Autowired ConfigurableApplicationContext context;

  @DisplayName("all default necessary beans should be created")
  @Test
  void contextLoads() {

    assertThat(AssertableApplicationContext.get(() -> context))
        .hasSingleBean(DebugProperties.class)
        .hasSingleBean(TemplateBuilder.class)
        .hasSingleBean(CacheManager.class)
        .hasSingleBean(JsonMapper.class)
        .hasSingleBean(GoogleTranslatorProperties.class)
        .hasSingleBean(DictionaryApiProperties.class)
        .hasSingleBean(ExternalApiProperties.class)
        .hasSingleBean(FilesProperties.class)
        .hasSingleBean(AnkiConverterCommand.class)
        .hasSingleBean(AnkiExporterCommand.class)
        .hasSingleBean(DictionaryApiCommand.class)
        .hasSingleBean(GoogleTranslateCommand.class)
        .hasSingleBean(TitleCommand.class)
        .hasSingleBean(VocabularyCommand.class)
        .hasSingleBean(TitleDaoJdbc.class)
        .hasSingleBean(VocabularyDaoJdbc.class)
        .hasSingleBean(AnkiDataFabricImpl.class)
        .hasSingleBean(AnkiConverterServiceCompletableFuture.class)
        .hasSingleBean(DictionaryServiceWebClient.class)
        .hasSingleBean(ExporterServiceAnkiCompletableFuture.class)
        .hasSingleBean(FileNameFormatterServiceImpl.class)
        .hasSingleBean(FileProviderServiceImpl.class)
        .hasSingleBean(GoogleTranslatorWebClient.class)
        .hasSingleBean(GoogleTranslatorParserImpl.class)
        .hasSingleBean(ObjectMapperServiceImpl.class)
        .hasSingleBean(StateServiceImpl.class)
        .hasSingleBean(StateServiceSqlite.class)
        .hasSingleBean(TemplateCreatorServiceImpl.class)
        .hasSingleBean(TitleServiceJdbc.class)
        .hasSingleBean(VocabularyServiceJdbc.class);
  }
}
