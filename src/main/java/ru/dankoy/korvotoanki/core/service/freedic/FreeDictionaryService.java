package ru.dankoy.korvotoanki.core.service.freedic;

import ru.dankoy.korvotoanki.core.domain.freedic.FreeDictionaryWord;

public interface FreeDictionaryService {

  FreeDictionaryWord define(String word);
}
