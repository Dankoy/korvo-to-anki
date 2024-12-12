package ru.dankoy.korvotoanki.core.service.googletrans;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;
import ru.dankoy.korvotoanki.config.WebClientConfig;
import ru.dankoy.korvotoanki.config.appprops.AppProperties;
import ru.dankoy.korvotoanki.config.appprops.GoogleParamsProperties;
import ru.dankoy.korvotoanki.config.appprops.GoogleTranslatorProperties;
import ru.dankoy.korvotoanki.core.domain.googletranslation.GoogleTranslation;
import ru.dankoy.korvotoanki.core.exceptions.GoogleTranslatorException;
import ru.dankoy.korvotoanki.core.service.googletrans.parser.GoogleTranslatorParser;

@DisplayName("Test GoogleTranslatorWebClient ")
@SpringBootTest(
    classes = {
      GoogleTranslatorWebClient.class,
      GoogleTranslatorProperties.class,
      GoogleParamsProperties.class,
      AppProperties.class,
      WebClient.class,
      WebClientConfig.class
    })
// @TestPropertySource(properties = "korvo-to-anki.http-client=web-client")
class GoogleTranslatorWebClientTest {

  private MockWebServer server;
  private String mockUrl = "http://127.0.0.1:%s/";

  @MockitoBean private GoogleTranslatorParser googleTranslatorParser;

  @MockitoBean private GoogleTranslatorProperties googleTranslatorProperties;

  @MockitoBean private GoogleParamsProperties properties;

  @Autowired private GoogleTranslatorWebClient googleTranslatorWebClient;

  @BeforeEach
  void setUpBeans() throws IOException {

    server = new MockWebServer();
    server.start();

    mockUrl = String.format(mockUrl, server.getPort());

    given(googleTranslatorProperties.getGoogleTranslatorUrl()).willReturn(mockUrl);
    given(googleTranslatorProperties.getGoogleParamsProperties()).willReturn(properties);
    given(properties.getClient()).willReturn("client");
    given(properties.getIe()).willReturn("UTF-8");
    given(properties.getOe()).willReturn("UTF-8");
    given(properties.getHl()).willReturn("en");
    given(properties.getOtf()).willReturn(0);
    given(properties.getSsel()).willReturn(0);
    given(properties.getTsel()).willReturn(0);

    given(googleTranslatorParser.parse(anyString())).willReturn(createGoogleTranslation());
  }

  @AfterEach
  void tearDownMockWebServer() throws IOException {
    server.shutdown();
  }

  @Test
  void testTranslationSuccess() throws InterruptedException {

    server.enqueue(
        new MockResponse().setBody("Some response").addHeader("Content-Type", "application/json"));

    GoogleTranslation translation =
        googleTranslatorWebClient.translate("text", "en", "ru", List.of("option1"));

    RecordedRequest recordedRequest = server.takeRequest();

    // do not assert hostname and port because localhost changes to 127.0.0.1 and 127.0.0.1 changes
    // to localhost. Always fails assertion.
    assertEquals(
        "/?client=client&sl=ru&tl=en&ie=UTF-8&oe=UTF-8&hl=en&otf=0&ssel=0&tsel=0&dt=option1&q=text",
        recordedRequest.getPath());

    assertThat(translation).isEqualTo(createGoogleTranslation());
  }

  @Test
  void testTranslationError_ExpectsGoogleTranslatorException() throws InterruptedException {

    server.enqueue(
        new MockResponse()
            .setResponseCode(400)
            .setBody("Some error")
            .addHeader("Content-Type", "application/json"));

    var list = List.of("option1");

    assertThatThrownBy(() -> googleTranslatorWebClient.translate("text", "en", "ru", list))
        .isInstanceOf(GoogleTranslatorException.class);

    RecordedRequest recordedRequest = server.takeRequest();

    // do not assert hostname and port because localhost changes to 127.0.0.1 and 127.0.0.1 changes
    // to localhost. Always fails assertion.
    assertEquals(
        "/?client=client&sl=ru&tl=en&ie=UTF-8&oe=UTF-8&hl=en&otf=0&ssel=0&tsel=0&dt=option1&q=text",
        recordedRequest.getPath());
  }

  @Test
  void testTranslationError_ExpectsParsingException() throws InterruptedException {

    server.enqueue(new MockResponse().setResponseCode(200).setBody("Some body"));

    given(googleTranslatorParser.parse(anyString()))
        .willThrow(new GoogleTranslatorException("some error", new RuntimeException()));

    var list = List.of("option1");

    assertThatThrownBy(() -> googleTranslatorWebClient.translate("text", "en", "ru", list))
        .isInstanceOf(GoogleTranslatorException.class);

    RecordedRequest recordedRequest = server.takeRequest();

    // do not assert hostname and port because localhost changes to 127.0.0.1 and 127.0.0.1 changes
    // to localhost. Always fails assertion.
    assertEquals(
        "/?client=client&sl=ru&tl=en&ie=UTF-8&oe=UTF-8&hl=en&otf=0&ssel=0&tsel=0&dt=option1&q=text",
        recordedRequest.getPath());
  }

  private GoogleTranslation createGoogleTranslation() {
    return new GoogleTranslation("Hello, world!");
  }
}
