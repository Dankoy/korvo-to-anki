package ru.dankoy.korvotoanki;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.dankoy.korvotoanki.config.OkHttpConfig;
import ru.dankoy.korvotoanki.config.appprops.AppProperties;
import ru.dankoy.korvotoanki.config.appprops.DictionaryApiProperties;
import ru.dankoy.korvotoanki.config.appprops.GoogleParamsProperties;
import ru.dankoy.korvotoanki.core.service.dictionaryapi.DictionaryServiceOkHttp;
import ru.dankoy.korvotoanki.core.service.googletrans.GoogleTranslatorOkHttp;
import ru.dankoy.korvotoanki.core.service.googletrans.parser.GoogleTranslatorParser;

@DisplayName("Test okhttp beans context ")
class KorvoToAnkiApplicationWebClientTests {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withInitializer(
              new ConditionEvaluationReportLoggingListener()) // to print out conditional config
          // report to log
          .withPropertyValues("debug=false")
          .withUserConfiguration(OkHttpConfig.class, Config.class)
          .withUserConfiguration(GoogleTranslatorOkHttp.class, DictionaryServiceOkHttp.class);

  @DisplayName("all okhttp service beans")
  @Test
  void contextLoadsOkHttpServiceBeansExpectsToLoad() {

    contextRunner
        // .withUserConfiguration(AppProperties.class)
        .withPropertyValues("korvo-to-anki.http-client=ok-http")
        .run(
            context ->
                assertAll(
                    () -> assertThat(context).hasSingleBean(GoogleTranslatorOkHttp.class),
                    () -> assertThat(context).hasSingleBean(DictionaryServiceOkHttp.class),
                    () -> assertThat(context).hasSingleBean(OkHttpConfig.class)));
  }

  @DisplayName("all okhttp service beans")
  @Test
  void contextLoadsOkHttpServiceBeansExpectsToNotLoad() {

    contextRunner
        // .withUserConfiguration(AppProperties.class)
        .withPropertyValues("korvo-to-anki.http-client=whatever")
        .run(
            context ->
                assertAll(
                    () -> assertThat(context).doesNotHaveBean(GoogleTranslatorOkHttp.class),
                    () -> assertThat(context).doesNotHaveBean(DictionaryServiceOkHttp.class),
                    () -> assertThat(context).doesNotHaveBean(OkHttpConfig.class)));
  }

  @Configuration
  protected static class Config {

    @Bean
    public AppProperties appProperties() {
      return Mockito.mock(AppProperties.class);
    }

    @Bean
    public GoogleParamsProperties googleParamsProperties() {
      return Mockito.mock(GoogleParamsProperties.class);
    }

    @Bean
    public GoogleTranslatorParser googleTranslatorParser() {
      return Mockito.mock(GoogleTranslatorParser.class);
    }

    @Bean
    public DictionaryApiProperties dictionaryApiProperties() {
      return Mockito.mock(DictionaryApiProperties.class);
    }

    @Bean
    public ObjectMapper objectMapper() {
      return Mockito.mock(ObjectMapper.class);
    }
  }
}
