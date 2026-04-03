package ru.dankoy.korvotoanki.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadPoolConfig {

  @Bean
  public ExecutorService ankiConverterTaskExecutor() {
    return Executors.newFixedThreadPool(2, Thread.ofVirtual().factory());
  }

  // This is necessary to gracefully shutdown executor service
  @Bean
  public DisposableBean shutdownExecutor(ExecutorService ankiConverterTaskExecutor) {
    return () -> {
      ankiConverterTaskExecutor.shutdown();
      if (!ankiConverterTaskExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
        ankiConverterTaskExecutor.shutdownNow();
      }
    };
  }
}
