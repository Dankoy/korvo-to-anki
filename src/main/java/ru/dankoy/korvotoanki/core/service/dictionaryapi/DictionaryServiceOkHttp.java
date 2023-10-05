package ru.dankoy.korvotoanki.core.service.dictionaryapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.dankoy.korvotoanki.config.appprops.DictionaryApiProperties;
import ru.dankoy.korvotoanki.core.domain.dictionaryapi.Word;
import ru.dankoy.korvotoanki.core.exceptions.DictionaryApiException;
import ru.dankoy.korvotoanki.core.exceptions.TooManyRequestsException;


@Slf4j
@RequiredArgsConstructor
@Service
public class DictionaryServiceOkHttp implements DictionaryService {

  private final OkHttpClient okHttpClient;

  private final DictionaryApiProperties dictionaryApiProperties;

  private final ObjectMapper mapper;

  @Cacheable(cacheManager = "cacheManager",
      value = "dictionaryApi",
      key = "#word"
  )
  @Override
  public List<Word> define(String word) {

    var url = Objects.requireNonNull(
            HttpUrl.parse(
                dictionaryApiProperties.getDictionaryApiUrl() + word
            )
        ).newBuilder()
        .build()
        .toString();

    var request = new Request.Builder()
        .url(url)
        .build();

    var call = okHttpClient.newCall(request);

    try (var response = call.execute()) {

      var body = receiveBody(response);
      checkStatus(response, body);

      if (body == null) {
        return Stream.of(Word.emptyWord()).toList();
      }
      return mapper.readValue(body, new TypeReference<List<Word>>() {
      });

    } catch (IOException e) {
      throw new DictionaryApiException(e);
    }
  }

  private String receiveBody(Response response) {

    try {
      return response.body() != null ? response.body().string() : null;
    } catch (Exception e) {
      log.debug("Unable to get response body");
    }

    return null;
  }

  private void checkStatus(Response response, String body) {

    if (!response.isSuccessful()) {

      if (response.code() == 429) {
        throw new TooManyRequestsException("Too many requests to dictionaryapi");
      }

      throw new DictionaryApiException(body);
    }

  }

}
