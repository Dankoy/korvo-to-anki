package ru.dankoy.korvotoanki.core.service.converter;

import java.util.List;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;

public interface AnkiConverterService {

  AnkiData convert(
      Vocabulary vocabulary, String sourceLanguage, String targetLanguage, List<String> options);
}
