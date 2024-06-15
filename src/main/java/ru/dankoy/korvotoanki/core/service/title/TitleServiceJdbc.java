package ru.dankoy.korvotoanki.core.service.title;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.dankoy.korvotoanki.core.dao.vocabularybuilder.title.TitleDao;
import ru.dankoy.korvotoanki.core.domain.Title;

@Service
@RequiredArgsConstructor
public class TitleServiceJdbc implements TitleService {

  private final TitleDao titleDao;

  @Override
  public List<Title> getAll() {
    return titleDao.getAll();
  }

  @Override
  public Title getById(long id) {
    return titleDao.getById(id);
  }

  @Override
  public Title getByName(String name) {
    return titleDao.getByName(name);
  }

  @Override
  public long insert(String titleName) {
    return titleDao.insert(titleName);
  }

  @Override
  public void deleteById(long id) {
    titleDao.deleteById(id);
  }

  @Override
  public void update(Title title) {
    titleDao.update(title);
  }

  @Override
  public long count() {
    return titleDao.count();
  }
}
