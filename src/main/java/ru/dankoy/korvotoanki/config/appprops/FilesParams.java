package ru.dankoy.korvotoanki.config.appprops;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "korvo-to-anki.files")
public class FilesParams implements FilesProperties {

  private final String exportFileName;
  private final String stateFileName;

}
