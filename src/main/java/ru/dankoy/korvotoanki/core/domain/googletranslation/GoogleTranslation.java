package ru.dankoy.korvotoanki.core.domain.googletranslation;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class GoogleTranslation {

  private final List<String> translations = new ArrayList<>();

  private final String transcription;

  private final List<Definition> definitions = new ArrayList<>();

  private final List<Synonym> synonyms = new ArrayList<>();

  private final List<String> examples = new ArrayList<>();

  // suggested correction when google translate thinks the source text has a typo, could be null
  private String possibleMistake;
}
