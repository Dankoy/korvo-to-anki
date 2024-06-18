package ru.dankoy.korvotoanki.core.dao.vocabularybuilder.vocabulary;

import java.util.List;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;

public interface VocabularyDao {

  List<Vocabulary> getAll();

  List<Vocabulary> getByTitle(Title title);

  long count();
}
