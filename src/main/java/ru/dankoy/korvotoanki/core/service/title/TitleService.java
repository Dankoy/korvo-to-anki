package ru.dankoy.korvotoanki.core.service.title;

import java.util.List;
import ru.dankoy.korvotoanki.core.domain.Title;

public interface TitleService {

  List<Title> getAll();

  Title getById(long id);

  Title getByName(String name);

  /**
   * Returns all books with the given names. Fails with {@link
   * ru.dankoy.korvotoanki.core.exceptions.KorvoRootException} when any of the names is not found in
   * the database.
   */
  List<Title> getAllByNames(List<String> names);

  long insert(String titleName);

  void deleteById(long id);

  void update(Title title);

  long count();
}
