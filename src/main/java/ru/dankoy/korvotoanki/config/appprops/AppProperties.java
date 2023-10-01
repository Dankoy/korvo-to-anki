package ru.dankoy.korvotoanki.config.appprops;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "korvo-to-anki")
public class AppProperties implements GoogleTranslatorProperties, DebugProperties,
    DictionaryApiProperties {

  private final String googleTranslatorUrl;

  private final String dictionaryApiUrl;

  @Autowired
  private final GoogleParamsProperties googleParamsProperties;


  @Value("${debug}")
  private final boolean debug;

}
