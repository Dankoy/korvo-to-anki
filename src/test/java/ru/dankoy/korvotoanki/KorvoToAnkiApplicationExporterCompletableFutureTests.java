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
import ru.dankoy.korvotoanki.config.IoServiceConfig;
import ru.dankoy.korvotoanki.config.appprops.FilesProperties;
import ru.dankoy.korvotoanki.core.service.converter.AnkiConverterService;
import ru.dankoy.korvotoanki.core.service.exporter.ExporterServiceAnkiCompletableFuture;
import ru.dankoy.korvotoanki.core.service.filenameformatter.FileNameFormatterService;
import ru.dankoy.korvotoanki.core.service.fileprovider.FileProviderService;
import ru.dankoy.korvotoanki.core.service.state.StateService;
import ru.dankoy.korvotoanki.core.service.templatecreator.TemplateCreatorService;
import ru.dankoy.korvotoanki.core.service.vocabulary.VocabularyService;

@DisplayName("Test completable future exporter bean context ")
class KorvoToAnkiApplicationExporterCompletableFutureTests {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withInitializer(
              new ConditionEvaluationReportLoggingListener()) // to print out conditional config
          // report to log
          .withUserConfiguration(TestConfig.class, IoServiceConfig.class)
          .withUserConfiguration(ExporterServiceAnkiCompletableFuture.class);

  @DisplayName("completable future exporter bean exists")
  @Test
  void asyncExporterServiceExists() {

    contextRunner
        .withPropertyValues(
            "korvo-to-anki.async=true", "korvo-to-anki.async-type=completable_future")
        .run(
            context ->
                assertAll(
                    () ->
                        assertThat(context)
                            .hasSingleBean(ExporterServiceAnkiCompletableFuture.class)));
  }

  @DisplayName("completable future exporter bean not exists")
  @Test
  void asyncExporterServiceNotExists() {

    contextRunner
        .withPropertyValues("korvo-to-anki.async=false")
        .run(
            context ->
                assertAll(
                    () ->
                        assertThat(context)
                            .doesNotHaveBean(ExporterServiceAnkiCompletableFuture.class)));
  }

  @DisplayName("completable future exporter bean not exists")
  @Test
  void asyncExporterServiceTypeWhateverNotExists() {

    contextRunner
        .withPropertyValues("korvo-to-anki.async=true", "korvo-to-anki.async-type=whatever")
        .run(
            context ->
                assertAll(
                    () ->
                        assertThat(context)
                            .doesNotHaveBean(ExporterServiceAnkiCompletableFuture.class)));
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

    @Bean
    public FileProviderService fileProviderService() {
      return Mockito.mock(
          FileProviderService.class); // this bean will be automatically autowired into tested beans
    }

    @Bean
    public FileNameFormatterService fileNameFormatterService() {
      return Mockito.mock(
          FileNameFormatterService
              .class); // this bean will be automatically autowired into tested beans
    }
  }
}
