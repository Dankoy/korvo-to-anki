package ru.dankoy.korvotoanki.config;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.dankoy.korvotoanki.config.appprops.DebugProperties;

@RequiredArgsConstructor
@Configuration
public class OkHttpConfig {

  private final DebugProperties debugProperties;

  @SuppressWarnings("KotlinInternalInJava")
  @Bean
  public OkHttpClient okHttpClient() {

    OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    httpClient.connectionPool(new ConnectionPool(10, 5, TimeUnit.SECONDS));
    httpClient.callTimeout(10L, TimeUnit.SECONDS);

    if (debugProperties.isDebug()) {
      var interceptor = new HttpLoggingInterceptor();
      interceptor.setLevel(Level.BODY);
      httpClient.interceptors().add(interceptor);
    }

    httpClient
        .interceptors()
        .add(new UserAgentInterceptor("Mozilla/5.0 (Windows NT 10.0; Win64; x64)"));

    return httpClient.build();
  }

  static class UserAgentInterceptor implements Interceptor {

    private final String userAgent;

    public UserAgentInterceptor(String userAgent) {
      this.userAgent = userAgent;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
      Request originalRequest = chain.request();
      Request requestWithUserAgent =
          originalRequest
              .newBuilder()
              .removeHeader("User-Agent")
              .addHeader("User-Agent", userAgent)
              .build();
      return chain.proceed(requestWithUserAgent);
    }
  }
}
