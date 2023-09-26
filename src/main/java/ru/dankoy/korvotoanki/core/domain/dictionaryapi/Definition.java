package ru.dankoy.korvotoanki.core.domain.dictionaryapi;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Definition {

  @JsonProperty("definition")
  private String info;
  private List<String> synonyms;
  private List<String> antonyms;
  private String example;

}
