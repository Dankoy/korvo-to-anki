package ru.dankoy.korvotoanki.core.domain.dictionaryapi;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Word {

  @JsonProperty("word")
  private String data;

  // could be null, but one of the Phonetics object could contain text with transcription
  private String phonetic;

  private List<Phonetics> phonetics;

  private List<Meaning> meanings;


  public static Word emptyWord() {
    return new Word(null, null, new ArrayList<>(), new ArrayList<>());
  }

}
