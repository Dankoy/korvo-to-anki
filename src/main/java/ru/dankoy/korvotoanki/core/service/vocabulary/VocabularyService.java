package ru.dankoy.korvotoanki.core.service.vocabulary;

import java.util.List;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;

public interface VocabularyService {

  List<Vocabulary> getAll();

  List<Vocabulary> getByTitle(Title title);

  /** Returns vocabularies of all given books. */
  List<Vocabulary> getByTitles(List<Title> titles);

  long count();
}
