package ru.dankoy.korvotoanki.core.dao.vocabularybuilder.title;

import java.util.List;
import ru.dankoy.korvotoanki.core.domain.Title;

public interface TitleDao {

  List<Title> getAll();

  Title getById(long id);

  Title getByName(String name);

  long insert(String titleName);

  void deleteById(long id);

  void update(Title title);

  long count();
}
