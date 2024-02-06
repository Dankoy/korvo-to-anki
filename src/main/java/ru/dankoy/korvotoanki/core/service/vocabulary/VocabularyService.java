package ru.dankoy.korvotoanki.core.service.vocabulary;

import java.util.List;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;

public interface VocabularyService {

  List<Vocabulary> getAll();

  List<Vocabulary> getByTitle(Title title);

  long count();
}
