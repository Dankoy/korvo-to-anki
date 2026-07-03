package ru.dankoy.korvotoanki.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class AppConfig {

  @Bean
  public JsonMapper jsonMapper() {
    return new JsonMapper();
  }
}
