package ru.dankoy.korvotoanki.core.service.dictionaryapi;

import java.util.List;
import ru.dankoy.korvotoanki.core.domain.dictionaryapi.Word;

public interface DictionaryService {

  List<Word> define(String word);

}
