package ru.dankoy.korvotoanki.core.domain.dictionaryapi;


import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class Meaning {

  private String partOfSpeech;

  private List<Definition> definitions;

  private List<String> synonyms;
  private List<String> antonyms;

}
