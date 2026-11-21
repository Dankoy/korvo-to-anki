package ru.dankoy.korvotoanki.core.domain.freedic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FreeDictionarySense {

  private String definition;

  private List<String> examples;

  private List<String> synonyms;

  private List<String> antonyms;

  private List<FreeDictionarySense> subsenses;
}
