package ru.dankoy.korvotoanki.core.service.googletrans;


import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import ru.dankoy.korvotoanki.config.GoogleTranslatorProperties;
import ru.dankoy.korvotoanki.core.exceptions.GoogleTranslatorException;


@Slf4j
@RequiredArgsConstructor
@Service
public class GoogleTranslatorOkHttp implements GoogleTranslator {

  private final OkHttpClient okHttpClient;

  private final GoogleTranslatorProperties googleTranslatorProperties;

  @Override
  public String translate(String text,
      String targetLanguage,
      String sourceLanguage,
      List<String> dtOptions
  ) {

    HttpUrl.Builder urlBuilder = Objects.requireNonNull(
        HttpUrl.parse(
            googleTranslatorProperties.getGoogleTranslatorUrl()
        )
    ).newBuilder();

    urlBuilder.addQueryParameter("client",
        googleTranslatorProperties.getGoogleParamsProperties().getClient());
    urlBuilder.addQueryParameter("sl", sourceLanguage);
    urlBuilder.addQueryParameter("tl", targetLanguage);
    urlBuilder.addQueryParameter("ie",
        googleTranslatorProperties.getGoogleParamsProperties().getIe());
    urlBuilder.addQueryParameter("oe",
        googleTranslatorProperties.getGoogleParamsProperties().getOe());
    urlBuilder.addQueryParameter("hl",
        googleTranslatorProperties.getGoogleParamsProperties().getHl());
    urlBuilder.addQueryParameter("otf",
        String.valueOf(googleTranslatorProperties.getGoogleParamsProperties().getOtf()));
    urlBuilder.addQueryParameter("ssel",
        String.valueOf(googleTranslatorProperties.getGoogleParamsProperties().getSsel()));
    urlBuilder.addQueryParameter("tsel",
        String.valueOf(googleTranslatorProperties.getGoogleParamsProperties().getTsel()));

    dtOptions.forEach(p -> urlBuilder.addQueryParameter("dt", p));

    urlBuilder.addQueryParameter("q", text);

    var url = urlBuilder.build().toString();

    var request = new Request.Builder()
        .url(url)
        .build();

    var call = okHttpClient.newCall(request);

    try (var response = call.execute()) {

      checkStatus(response);
      return response.body() != null ? response.body().string() : "";

    } catch (Exception e) {
      throw new GoogleTranslatorException(e);
    }

  }

  private void checkStatus(Response response) {

    if (!response.isSuccessful()) {
      log.error("Something went wrong");
      throw new GoogleTranslatorException("Response is not successfull",
          new RuntimeException(String.valueOf(response.body())));
    }

  }

}