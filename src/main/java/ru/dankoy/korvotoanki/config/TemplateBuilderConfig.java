package ru.dankoy.korvotoanki.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.dankoy.korvotoanki.core.service.templatebuilder.TemplateBuilder;
import ru.dankoy.korvotoanki.core.service.templatebuilder.TemplateBuilderImpl;

@Configuration
public class TemplateBuilderConfig {


  @Bean
  TemplateBuilder templateBuilder(@Value("${spring.freemarker.template-loader-path}") String templatesDir) {
    return new TemplateBuilderImpl(templatesDir);
  }

}
