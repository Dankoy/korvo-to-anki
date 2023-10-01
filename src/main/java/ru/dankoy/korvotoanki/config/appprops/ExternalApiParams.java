package ru.dankoy.korvotoanki.config.appprops;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "korvo-to-anki.api")
public class ExternalApiParams implements ExternalApiProperties {

  private final boolean dictionaryApiEnabled;

}
