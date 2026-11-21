package ru.dankoy.korvotoanki.core.service.freedic;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.dankoy.korvotoanki.config.appprops.FreeDictionaryApiProperties;
import ru.dankoy.korvotoanki.core.domain.freedic.FreeDictionaryWord;
import ru.dankoy.korvotoanki.core.exceptions.FreeDictionaryApiException;
import ru.dankoy.korvotoanki.core.exceptions.TooManyRequestsException;

@ConditionalOnProperty(prefix = "korvo-to-anki", value = "http-client", havingValue = "web-client")
@Slf4j
@RequiredArgsConstructor
@Service
public class FreeDictionaryServiceWebClient implements FreeDictionaryService {

  private final WebClient webClient;

  private final FreeDictionaryApiProperties freeDictionaryApiProperties;

  @Retry(name = "free-dictionary-api")
  @RateLimiter(name = "free-dictionary-api")
  @Cacheable(cacheManager = "cacheManager", value = "freeDictionaryApi", key = "#word")
  @Override
  public FreeDictionaryWord define(String word) {

    var url =
        Objects.requireNonNull(
                HttpUrl.parse(freeDictionaryApiProperties.getFreeDictionaryApiUrl() + word))
            .newBuilder()
            .build()
            .toString();

    return webClient
        .get()
        .uri(url)
        .retrieve()
        .onRawStatus(status -> status == 429, this::handleTooManyRequestsError)
        .onStatus(HttpStatusCode::isError, this::handleAnyResponseError)
        .bodyToMono(FreeDictionaryWord.class)
        .defaultIfEmpty(FreeDictionaryWord.emptyWord())
        .block();
  }

  private Mono<? extends Throwable> handleTooManyRequestsError(ClientResponse clientResponse) {
    return Mono.error(new TooManyRequestsException("Too many requests to freedictionaryapi"));
  }

  private Mono<? extends Throwable> handleAnyResponseError(ClientResponse clientResponse) {
    return clientResponse
        .bodyToMono(String.class)
        .flatMap(body -> Mono.error(new FreeDictionaryApiException(body)));
  }
}
