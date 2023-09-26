package ru.dankoy.korvotoanki.config.appprops;


import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "korvo-to-anki.trans-params")
public class GoogleParamsProperties {

  private final String client;
  private final String ie;
  private final String oe;
  private final String sl;
  private final String tl;
  private final String hl;
  private final int otf;
  private final int ssel;
  private final int tsel;
  private final List<String> dt;

}
