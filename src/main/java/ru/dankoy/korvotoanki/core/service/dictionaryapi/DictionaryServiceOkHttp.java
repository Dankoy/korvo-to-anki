package ru.dankoy.korvotoanki.core.service.dictionaryapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;
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

      checkStatus(response);
      var body = response.body() != null ? response.body().string() : "{}";

      return mapper.readValue(body, new TypeReference<List<Word>>() {
      });

    } catch (Exception e) {
      throw new DictionaryApiException(e);
    }
  }

  private void checkStatus(Response response) {

    if (!response.isSuccessful()) {
      log.error("Something went wrong");
      throw new DictionaryApiException("Response is not success",
          new RuntimeException(String.valueOf(response.body())));
    }

  }

}
