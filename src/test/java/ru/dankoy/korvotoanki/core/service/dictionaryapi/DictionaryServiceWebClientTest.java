package ru.dankoy.korvotoanki.core.service.dictionaryapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;
import ru.dankoy.korvotoanki.config.WebClientConfig;
import ru.dankoy.korvotoanki.config.appprops.AppProperties;
import ru.dankoy.korvotoanki.config.appprops.DictionaryApiProperties;
import ru.dankoy.korvotoanki.core.domain.dictionaryapi.Phonetics;
import ru.dankoy.korvotoanki.core.domain.dictionaryapi.Word;
import ru.dankoy.korvotoanki.core.exceptions.DictionaryApiException;

@DisplayName("Test DictionaryServiceWebClient ")
@SpringBootTest(
    classes = {
      WebClient.class,
      WebClientConfig.class,
      AppProperties.class,
      ObjectMapper.class,
      DictionaryServiceWebClient.class
    })
@TestPropertySource(properties = "korvo-to-anki.http-client=web-client")
class DictionaryServiceWebClientTest {

  private static MockWebServer server;
  private String mockUrl = "";

  @BeforeAll
  static void setUp() throws IOException {
    server = new MockWebServer();
    server.start();
  }

  @AfterAll
  static void tearDown() throws IOException {
    server.shutdown();
  }

  @MockitoBean private DictionaryApiProperties dictionaryApiProperties;

  @Autowired private DictionaryServiceWebClient dictionaryService;

  @Autowired private ObjectMapper mapper;

  @BeforeEach
  void initialize() {
    mockUrl = String.format("http://localhost:%s/", server.getPort());
  }

  @DisplayName("correct translation")
  @Test
  void defineCorrectWord() throws IOException {

    var word = "word";
    given(dictionaryApiProperties.getDictionaryApiUrl()).willReturn(mockUrl);

    server.enqueue(
        new MockResponse()
            .setBody(mapper.writeValueAsString(getWords(false)))
            .addHeader("Content-Type", "application/json"));

    List<Word> actual = dictionaryService.define(word);

    assertThat(actual).isEqualTo(getWords(false));

    Mockito.verify(dictionaryApiProperties, times(1)).getDictionaryApiUrl();
  }

  @DisplayName("definition throws exception")
  @Test
  void defineNonCorrectWordThrowsException() throws IOException {

    var word = "exception_word";
    given(dictionaryApiProperties.getDictionaryApiUrl()).willReturn(mockUrl);

    server.enqueue(
        new MockResponse()
            .setBody(mapper.writeValueAsString(getBody(false)))
            .setResponseCode(404)
            .addHeader("Content-Type", "application/json"));

    assertThatThrownBy(() -> dictionaryService.define(word))
        .isInstanceOf(DictionaryApiException.class);

    Mockito.verify(dictionaryApiProperties, times(1)).getDictionaryApiUrl();
  }

  @DisplayName("definition response body is null")
  @Test
  void defineResponseBodyNull() {

    given(dictionaryApiProperties.getDictionaryApiUrl()).willReturn(mockUrl);
    var word = "exception_word";

    server.enqueue(new MockResponse().setBody("").addHeader("Content-Type", "application/json"));

    List<Word> actual = dictionaryService.define(word);

    assertThat(actual).isEqualTo(getWords(true));

    Mockito.verify(dictionaryApiProperties, times(1)).getDictionaryApiUrl();
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
