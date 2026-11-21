package ru.dankoy.korvotoanki.core.service.freedic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.io.IOException;
import java.util.List;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.netty.http.client.HttpClient;
import ru.dankoy.korvotoanki.config.appprops.FreeDictionaryApiProperties;
import ru.dankoy.korvotoanki.core.domain.freedic.FreeDictionaryEntry;
import ru.dankoy.korvotoanki.core.domain.freedic.FreeDictionarySense;
import ru.dankoy.korvotoanki.core.domain.freedic.FreeDictionaryWord;
import ru.dankoy.korvotoanki.core.domain.freedic.Pronunciation;
import ru.dankoy.korvotoanki.core.exceptions.FreeDictionaryApiException;
import tools.jackson.databind.json.JsonMapper;

@DisplayName("Test FreeDictionaryServiceWebClient ")
@SpringBootTest(
    classes = {
      WebClient.class,
      FreeDictionaryServiceWebClientTest.WebClientConfig.class,
      JsonMapper.class,
      FreeDictionaryServiceWebClient.class
    })
class FreeDictionaryServiceWebClientTest {

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

  @MockitoBean private FreeDictionaryApiProperties freeDictionaryApiProperties;

  @Autowired private FreeDictionaryServiceWebClient freeDictionaryService;

  @DisplayName("correct definition")
  @Test
  void defineCorrectWord() throws IOException, InterruptedException {

    var word = "word";
    given(freeDictionaryApiProperties.getFreeDictionaryApiUrl()).willReturn(mockUrl);

    server.enqueue(
        new MockResponse().setBody(getBody()).addHeader("Content-Type", "application/json"));

    var expected = getFreeDictionaryWord();
    FreeDictionaryWord actual = freeDictionaryService.define(word, "en");

    assertThat(actual).isEqualTo(expected);

    var req = server.takeRequest();
    assertThat(req.getMethod()).isEqualTo("GET");
    assertThat(req.getPath().substring(1)).isEqualTo("en/" + word);

    Mockito.verify(freeDictionaryApiProperties, times(1)).getFreeDictionaryApiUrl();
  }

  @DisplayName("word not found returns empty entries")
  @Test
  void defineNonCorrectWordReturnsEmptyEntries() throws IOException, InterruptedException {

    var word = "exception_word";
    given(freeDictionaryApiProperties.getFreeDictionaryApiUrl()).willReturn(mockUrl);

    server.enqueue(
        new MockResponse()
            .setBody("{\"word\":\"exception_word\",\"entries\":[]}")
            .addHeader("Content-Type", "application/json"));

    FreeDictionaryWord actual = freeDictionaryService.define(word, "en");

    assertThat(actual.getEntries()).isEmpty();

    var req = server.takeRequest();
    assertThat(req.getMethod()).isEqualTo("GET");
    assertThat(req.getPath().substring(1)).isEqualTo("en/" + word);

    Mockito.verify(freeDictionaryApiProperties, times(1)).getFreeDictionaryApiUrl();
  }

  @DisplayName("definition throws exception on error status")
  @Test
  void defineNonCorrectWordThrowsException() throws IOException, InterruptedException {

    var word = "exception_word";
    given(freeDictionaryApiProperties.getFreeDictionaryApiUrl()).willReturn(mockUrl);

    server.enqueue(
        new MockResponse()
            .setBody("{\"error\":\"internal\"}")
            .setResponseCode(500)
            .addHeader("Content-Type", "application/json"));

    assertThatThrownBy(() -> freeDictionaryService.define(word, "en"))
        .isInstanceOf(FreeDictionaryApiException.class);

    var req = server.takeRequest();
    assertThat(req.getMethod()).isEqualTo("GET");
    assertThat(req.getPath().substring(1)).isEqualTo("en/" + word);

    Mockito.verify(freeDictionaryApiProperties, times(1)).getFreeDictionaryApiUrl();
  }

  @DisplayName("definition read timeout")
  @Test
  void defineReadTimeout() throws InterruptedException {

    given(freeDictionaryApiProperties.getFreeDictionaryApiUrl()).willReturn(mockUrl);
    var word = "exception_word";

    server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

    assertThatThrownBy(() -> freeDictionaryService.define(word, "de"))
        .isInstanceOf(WebClientRequestException.class)
        .hasCauseInstanceOf(ReadTimeoutException.class);

    var req = server.takeRequest();
    assertThat(req.getMethod()).isEqualTo("GET");
    assertThat(req.getPath().substring(1)).isEqualTo("de/" + word);

    Mockito.verify(freeDictionaryApiProperties, times(1)).getFreeDictionaryApiUrl();
  }

  private FreeDictionaryWord getFreeDictionaryWord() {

    return new FreeDictionaryWord(
        "word",
        List.of(
            new FreeDictionaryEntry(
                "noun",
                List.of(new Pronunciation("ipa", "/transcription/", List.of("UK"))),
                List.of(
                    new FreeDictionarySense(
                        "definition",
                        List.of("example"),
                        List.of("synonym"),
                        List.of("antonym"),
                        List.of(
                            new FreeDictionarySense(
                                "subsense", List.of(), List.of(), List.of(), List.of())))),
                List.of("entry_synonym"),
                List.of("entry_antonym"))));
  }

  private String getBody() {

    return """
    {"word":"word","entries":[{"language":{"code":"en","name":"English"},\
    "partOfSpeech":"noun",\
    "pronunciations":[{"type":"ipa","text":"/transcription/","tags":["UK"]}],\
    "forms":[],\
    "senses":[{"definition":"definition","tags":[],"examples":["example"],\
    "quotes":[],"synonyms":["synonym"],"antonyms":["antonym"],\
    "subsenses":[{"definition":"subsense","tags":[],"examples":[],\
    "quotes":[],"synonyms":[],"antonyms":[],"subsenses":[]}]}],\
    "synonyms":["entry_synonym"],"antonyms":["entry_antonym"]}],\
    "source":{"url":"https://en.wiktionary.org","license":{"name":"CC BY-SA 4.0"}}}\
    """;
  }

  @Configuration
  static class WebClientConfig {
    @Bean
    public WebClient webClientWithTimeout() {

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
}
