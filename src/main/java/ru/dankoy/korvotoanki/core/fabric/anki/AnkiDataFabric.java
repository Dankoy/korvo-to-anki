package ru.dankoy.korvotoanki.core.fabric.anki;

import java.util.List;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;
import ru.dankoy.korvotoanki.core.domain.dictionaryapi.Word;
import ru.dankoy.korvotoanki.core.domain.googletranslation.GoogleTranslation;

public interface AnkiDataFabric {

  AnkiData createAnkiData(
      Vocabulary vocabulary, GoogleTranslation googleTranslation, List<Word> word);
}
