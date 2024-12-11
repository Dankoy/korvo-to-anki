package ru.dankoy.korvotoanki.core.service.googletrans;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import ru.dankoy.korvotoanki.config.WebClientConfig;
import ru.dankoy.korvotoanki.config.appprops.AppProperties;
import ru.dankoy.korvotoanki.config.appprops.GoogleParamsProperties;
import ru.dankoy.korvotoanki.config.appprops.GoogleTranslatorProperties;
import ru.dankoy.korvotoanki.core.domain.googletranslation.GoogleTranslation;
import ru.dankoy.korvotoanki.core.exceptions.GoogleTranslatorException;
import ru.dankoy.korvotoanki.core.service.googletrans.parser.GoogleTranslatorParser;

@DisplayName("Test GoogleTranslatorWebClient ")
@SpringBootTest(classes = {
        WebClient.class,
        WebClientConfig.class,
        AppProperties.class,
        ObjectMapper.class,
        GoogleTranslatorWebClient.class,
        GoogleTranslatorProperties.class
})
@TestPropertySource(properties = "korvo-to-anki.http-client=web-client")
class GoogleTranslatorWebClientTest {

    private static MockWebServer server;
    private static String mockUrl = "";

    @BeforeAll
    static void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        mockUrl = String.format("http://127.0.0.1:%s/", server.getPort());
    }

    @AfterAll
    static void tearDown() throws IOException {
        server.shutdown();
    }

    @MockitoBean
    private GoogleTranslatorParser googleTranslatorParser;

    @MockitoBean
    private GoogleTranslatorProperties googleTranslatorProperties;

    @MockitoBean
    private GoogleParamsProperties properties;

    @Autowired
    private GoogleTranslatorWebClient googleTranslatorWebClient;

    @BeforeEach
    void setUpBeans() {

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

    @Test
    void testTranslationSuccess() throws InterruptedException {

        server.enqueue(
                new MockResponse()
                        .setBody("Some response")
                        .addHeader("Content-Type", "application/json"));

        GoogleTranslation translation = googleTranslatorWebClient.translate("text", "en", "ru",
                List.of("option1"));

        RecordedRequest recordedRequest = server.takeRequest();

        assertEquals(mockUrl +
                "?client=client&sl=ru&tl=en&ie=UTF-8&oe=UTF-8&hl=en&otf=0&ssel=0&tsel=0&dt=option1&q=text",
                recordedRequest.getRequestUrl().toString());

        assertThat(translation).isEqualTo(createGoogleTranslation());
    }

    @Test
    void testTranslationError_ExpectsGoogleTranslatorException() throws InterruptedException {

        server.enqueue(
                new MockResponse()
                        .setResponseCode(400)
                        .setBody("Some error")
                        .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> googleTranslatorWebClient.translate("text", "en", "ru", List.of("option1")))
                .isInstanceOf(GoogleTranslatorException.class);

        RecordedRequest recordedRequest = server.takeRequest();

        assertEquals(mockUrl +
                "?client=client&sl=ru&tl=en&ie=UTF-8&oe=UTF-8&hl=en&otf=0&ssel=0&tsel=0&dt=option1&q=text",
                recordedRequest.getRequestUrl().toString());

    }

    @Test
    void testTranslationError_ExpectsParsingException() throws InterruptedException {

        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody("Some body"));

        given(googleTranslatorParser.parse(anyString()))
                .willThrow(new GoogleTranslatorException("some error", new RuntimeException()));

        assertThatThrownBy(() -> googleTranslatorWebClient.translate("text", "en",
                "ru", List.of("option1")))
                .isInstanceOf(GoogleTranslatorException.class);

        RecordedRequest recordedRequest = server.takeRequest();

        assertEquals(mockUrl +
                "?client=client&sl=ru&tl=en&ie=UTF-8&oe=UTF-8&hl=en&otf=0&ssel=0&tsel=0&dt=option1&q=text",
                recordedRequest.getRequestUrl().toString());

    }

    private GoogleTranslation createGoogleTranslation() {
        return new GoogleTranslation("Hello, world!");

    }

}
