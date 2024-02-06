package ru.dankoy.korvotoanki.core.domain.dictionaryapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Phonetics {

  private String text;
  private String audio;
  private String sourceUrl;
}
