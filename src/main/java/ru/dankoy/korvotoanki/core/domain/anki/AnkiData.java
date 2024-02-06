package ru.dankoy.korvotoanki.core.domain.anki;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class AnkiData {

  private final String word;
  private final String myExample;
  private final List<String> translations; // from google translate
  private final String
      transcription; // from dictionaryapi, if not found then from Google Translate or null
  private final List<Meaning>
      meanings; // from dictionaryapi, if not found then from Google Translate or empty
  private final String book;
}
