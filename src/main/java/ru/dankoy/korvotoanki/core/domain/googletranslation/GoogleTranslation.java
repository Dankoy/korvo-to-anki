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

}
