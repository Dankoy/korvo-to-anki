package ru.dankoy.korvotoanki.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadPoolConfig {

  @Bean(destroyMethod = "shutdownNow")
  public ExecutorService ankiConverterTaskExecutor() {
    return Executors.newFixedThreadPool(2);
  }
}
