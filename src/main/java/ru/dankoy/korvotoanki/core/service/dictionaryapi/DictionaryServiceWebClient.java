package ru.dankoy.korvotoanki.core.service.dictionaryapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
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
import ru.dankoy.korvotoanki.config.appprops.DictionaryApiProperties;
import ru.dankoy.korvotoanki.core.domain.dictionaryapi.Word;
import ru.dankoy.korvotoanki.core.exceptions.DictionaryApiException;
import ru.dankoy.korvotoanki.core.exceptions.TooManyRequestsException;

@ConditionalOnProperty(prefix = "korvo-to-anki", value = "http-client", havingValue = "web-client")
@Slf4j
@RequiredArgsConstructor
@Service
public class DictionaryServiceWebClient implements DictionaryService {

  private final WebClient webClient;

  private final DictionaryApiProperties dictionaryApiProperties;

  private final ObjectMapper mapper;

  @RateLimiter(name = "dictionary-api")
  @Cacheable(cacheManager = "cacheManager", value = "dictionaryApi", key = "#word")
  @Override
  public List<Word> define(String word) {

    var url =
        Objects.requireNonNull(HttpUrl.parse(dictionaryApiProperties.getDictionaryApiUrl() + word))
            .newBuilder()
            .build()
            .toString();

    return webClient
        .get()
        .uri(url)
        .retrieve()
        .onRawStatus(
            status -> status == 429,
            error -> Mono.error(new TooManyRequestsException("Too many requests to dictionaryapi")))
        .onStatus(
            HttpStatusCode::isError,
            error ->
                error
                    .bodyToMono(String.class)
                    .flatMap(body -> Mono.error(new DictionaryApiException(body))))
        .bodyToFlux(Word.class)
        .defaultIfEmpty(Word.emptyWord())
        .collectList()
        .block();
  }
}
