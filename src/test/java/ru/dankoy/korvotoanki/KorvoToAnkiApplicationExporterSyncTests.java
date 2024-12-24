package ru.dankoy.korvotoanki;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.dankoy.korvotoanki.config.appprops.FilesProperties;
import ru.dankoy.korvotoanki.core.service.converter.AnkiConverterService;
import ru.dankoy.korvotoanki.core.service.exporter.ExporterServiceAnki;
import ru.dankoy.korvotoanki.core.service.state.StateService;
import ru.dankoy.korvotoanki.core.service.templatecreator.TemplateCreatorService;
import ru.dankoy.korvotoanki.core.service.vocabulary.VocabularyService;

@DisplayName("Test sync exporter bean context ")
class KorvoToAnkiApplicationExporterSyncTests {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withInitializer(
              new ConditionEvaluationReportLoggingListener()) // to print out conditional config
          // report to log
          .withUserConfiguration(TestConfig.class)
          .withUserConfiguration(ExporterServiceAnki.class);

  @DisplayName("sync exporter bean exists")
  @Test
  void syncExporterServiceExists() {

    contextRunner
        .withPropertyValues("korvo-to-anki.async=false")
        .run(
            context ->
                assertAll(() -> assertThat(context).hasSingleBean(ExporterServiceAnki.class)));
  }

  @DisplayName("sync exporter bean not exists")
  @Test
  void syncExporterServiceNotExists() {

    contextRunner
        .withPropertyValues("korvo-to-anki.async=true")
        .run(
            context ->
                assertAll(() -> assertThat(context).doesNotHaveBean(ExporterServiceAnki.class)));
  }

  @Configuration // this annotation is not required here as the class is explicitly mentioned in
  // `withUserConfiguration` method
  protected static class TestConfig {
    @Bean
    public VocabularyService vocabularyService() {
      return Mockito.mock(
          VocabularyService.class); // this bean will be automatically autowired into tested beans
    }

    @Bean
    public AnkiConverterService ankiConverterService() {
      return Mockito.mock(
          AnkiConverterService
              .class); // this bean will be automatically autowired into tested beans
    }

    @Bean
    public TemplateCreatorService templateCreatorService() {
      return Mockito.mock(
          TemplateCreatorService
              .class); // this bean will be automatically autowired into tested beans
    }

    @Bean
    public FilesProperties filesProperties() {
      return Mockito.mock(
          FilesProperties.class); // this bean will be automatically autowired into tested beans
    }

    @Bean
    public StateService stateService() {
      return Mockito.mock(
          StateService.class); // this bean will be automatically autowired into tested beans
    }
  }
}
