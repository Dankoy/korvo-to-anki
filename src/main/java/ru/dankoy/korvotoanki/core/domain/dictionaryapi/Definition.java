package ru.dankoy.korvotoanki.core.domain.dictionaryapi;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Definition {

  @JsonProperty("definition")
  private String info;
  private List<String> synonyms;
  private List<String> antonyms;
  private String example;

}
