package ru.dankoy.korvotoanki.core.service.vocabulary;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.dankoy.korvotoanki.core.dao.vocabularybuilder.vocabulary.VocabularyDao;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;

@Service
@RequiredArgsConstructor
public class VocabularyServiceJdbc implements VocabularyService {

  private final VocabularyDao vocabularyDao;

  @Override
  public List<Vocabulary> getAll() {
    return vocabularyDao.getAll();
  }

  @Override
  public List<Vocabulary> getByTitle(Title title) {
    return vocabularyDao.getByTitle(title);
  }

  @Override
  public long count() {
    return vocabularyDao.count();
  }
}
