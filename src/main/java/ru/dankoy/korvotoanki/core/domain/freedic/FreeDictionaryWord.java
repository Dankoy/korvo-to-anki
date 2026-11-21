package ru.dankoy.korvotoanki.core.domain.freedic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FreeDictionaryWord {

  private String word;

  private List<FreeDictionaryEntry> entries;

  public static FreeDictionaryWord emptyWord() {
    return new FreeDictionaryWord(null, new ArrayList<>());
  }
}
