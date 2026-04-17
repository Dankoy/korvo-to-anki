package ru.dankoy.korvotoanki.core.service.dictionaryapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.netty.http.client.HttpClient;
import ru.dankoy.korvotoanki.KorvoToAnkiApplication;
import ru.dankoy.korvotoanki.config.appprops.DictionaryApiProperties;
import ru.dankoy.korvotoanki.config.appprops.GoogleTranslatorProperties;
import ru.dankoy.korvotoanki.core.exceptions.DictionaryApiException;
import tools.jackson.databind.ObjectMapper;

@DisplayName("Test DictionaryServiceWebClient with full contexst and resilience ")
@Import(DictionaryServiceWebClientTestResilience.WebClientConfigInternal.class)
@SpringBootTest(classes = {KorvoToAnkiApplication.class})
@EnableAutoConfiguration(
    exclude = {
      org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration.class,
      org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration.class
    })
// @DirtiesContext(classMode = ClassMode.AFTER_CLASS)

public class DictionaryServiceWebClientTestResilience {

  @TestConfiguration
  static class WebClientConfigInternal {

    @Primary
    @Bean
    public WebClient webClientWithTimeoutTest() {

      final var httpClient =
          HttpClient.create()
              .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
              .doOnConnected(
                  connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(1000, TimeUnit.MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(1000, TimeUnit.MILLISECONDS));
                  });

      return WebClient.builder()
          .clientConnector(new ReactorClientHttpConnector(httpClient))
          .defaultHeaders(
              httpHeaders -> {
                httpHeaders.set(
                    HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
              })
          .build();
    }
  }

  private MockWebServer server;
  private String mockUrl = "http://127.0.0.1:%s/";

  @BeforeEach
  void setUp() throws IOException {
    server = new MockWebServer();
    server.start();
    mockUrl = String.format(mockUrl, server.getPort());
  }

  @AfterEach
  void tearDown() throws IOException {
    server.shutdown();
  }

  @MockitoBean private DictionaryApiProperties dictionaryApiProperties;

  @MockitoBean private GoogleTranslatorProperties googleTranslatorProperties;

  @Autowired private DictionaryServiceWebClient dictionaryService;

  @MockitoSpyBean private WebClient webClientWithTimeoutTest;

  @Autowired private ObjectMapper mapper;

  @DisplayName("definition read timeout expects retry and the throw WebClientRequestException")
  @Test
  void defineReadTimeout_expectRetries() throws InterruptedException {

    given(dictionaryApiProperties.getDictionaryApiUrl()).willReturn(mockUrl);
    var word = "exception_word";

    server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

    assertThatThrownBy(() -> dictionaryService.define(word))
        .isInstanceOf(WebClientRequestException.class);

    var req = server.takeRequest();
    assertThat(req.getMethod()).isEqualTo("GET");
    assertThat(req.getPath().substring(1)).isEqualTo(word);

    Mockito.verify(webClientWithTimeoutTest, times(3)).get();
    Mockito.verify(dictionaryApiProperties, times(3)).getDictionaryApiUrl();
  }

  @DisplayName("definition read timeout expect no retries and throw DictionaryApiException")
  @Test
  void defineReadTimeout_expectNoRetries() throws InterruptedException {

    given(dictionaryApiProperties.getDictionaryApiUrl()).willReturn(mockUrl);
    var word = "exception_word";

    server.enqueue(
        new MockResponse()
            .setBody(mapper.writeValueAsString(getBody(false)))
            .setResponseCode(404)
            .addHeader("Content-Type", "application/json"));

    assertThatThrownBy(() -> dictionaryService.define(word))
        .isInstanceOf(DictionaryApiException.class);

    var req = server.takeRequest();
    assertThat(req.getMethod()).isEqualTo("GET");
    assertThat(req.getPath().substring(1)).isEqualTo(word);

    Mockito.verify(webClientWithTimeoutTest, times(1)).get();
    Mockito.verify(dictionaryApiProperties, times(1)).getDictionaryApiUrl();
  }

  private String getBody(boolean isCorrect) {

    if (isCorrect) {
      return "[{\"word\":\"data\",\"phonetic\":\"phonetic\",\"phonetics\":[{\"text\":\"text\",\"audio\":\"audio\",\"sourceUrl\":\"source\"}],\"meanings\":[{\"partOfSpeech\":\"ps\",\"definitions\":[{\"definition\":\"info\",\"synonyms\":[\"synonym1\"],\"antonyms\":[\"antonym1\"],\"example\":\"example\"}],\"synonyms\":[\"synonym1\"],\"antonyms\":[\"antonym1\"]}]}]";
    } else {
      return "{\"title\":\"No Definitions Found\",\"message\":\"Sorry pal, we couldn't find"
          + " definitions for the word you were looking for.\",\"resolution\":\"You can try"
          + " the search again at later time or head to the web instead.\"}";
    }
  }
}
