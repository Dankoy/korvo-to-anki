package ru.dankoy.korvotoanki.core.service.googletrans;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.dankoy.korvotoanki.config.appprops.GoogleTranslatorProperties;
import ru.dankoy.korvotoanki.core.domain.googletranslation.GoogleTranslation;
import ru.dankoy.korvotoanki.core.exceptions.GoogleTranslatorException;
import ru.dankoy.korvotoanki.core.service.googletrans.parser.GoogleTranslatorParser;

@ConditionalOnProperty(prefix = "korvo-to-anki", value = "http-client", havingValue = "web-client")
@Slf4j
@RequiredArgsConstructor
@Service
public class GoogleTranslatorWebClient implements GoogleTranslator {

  private final WebClient webClient;

  private final GoogleTranslatorProperties googleTranslatorProperties;

  private final GoogleTranslatorParser googleTranslatorParser;

  @Cacheable(
      cacheManager = "cacheManager",
      value = "googleTranslations",
      key =
          "#text + #targetLanguage + #sourceLanguage +"
              + " T(java.lang.Integer).toString(#dtOptions.hashCode())")
  @Override
  public GoogleTranslation translate(
      String text, String targetLanguage, String sourceLanguage, List<String> dtOptions) {

    HttpUrl.Builder urlBuilder =
        Objects.requireNonNull(HttpUrl.parse(googleTranslatorProperties.getGoogleTranslatorUrl()))
            .newBuilder();

    urlBuilder.addQueryParameter(
        "client", googleTranslatorProperties.getGoogleParamsProperties().getClient());
    urlBuilder.addQueryParameter("sl", sourceLanguage);
    urlBuilder.addQueryParameter("tl", targetLanguage);
    urlBuilder.addQueryParameter(
        "ie", googleTranslatorProperties.getGoogleParamsProperties().getIe());
    urlBuilder.addQueryParameter(
        "oe", googleTranslatorProperties.getGoogleParamsProperties().getOe());
    urlBuilder.addQueryParameter(
        "hl", googleTranslatorProperties.getGoogleParamsProperties().getHl());
    urlBuilder.addQueryParameter(
        "otf", String.valueOf(googleTranslatorProperties.getGoogleParamsProperties().getOtf()));
    urlBuilder.addQueryParameter(
        "ssel", String.valueOf(googleTranslatorProperties.getGoogleParamsProperties().getSsel()));
    urlBuilder.addQueryParameter(
        "tsel", String.valueOf(googleTranslatorProperties.getGoogleParamsProperties().getTsel()));

    dtOptions.forEach(p -> urlBuilder.addQueryParameter("dt", p));

    urlBuilder.addQueryParameter("q", text);

    var url = urlBuilder.build().toString();

    return webClient
        .get()
        .uri(url)
        .retrieve()
        .onStatus(
            HttpStatusCode::isError,
            error ->
                error
                    .bodyToMono(String.class)
                    .flatMap(
                        body ->
                            Mono.error(
                                new GoogleTranslatorException(
                                    "Response is not successful", new RuntimeException(body)))))
        .bodyToMono(String.class)
        .map(googleTranslatorParser::parse)
        .block();
  }
}
