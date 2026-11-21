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
public class FreeDictionaryEntry {

  private String partOfSpeech;

  private List<Pronunciation> pronunciations;

  private List<FreeDictionarySense> senses;

  private List<String> synonyms;

  private List<String> antonyms;
}
