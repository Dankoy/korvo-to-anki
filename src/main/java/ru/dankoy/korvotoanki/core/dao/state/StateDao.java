package ru.dankoy.korvotoanki.core.dao.state;

import java.util.List;
import ru.dankoy.korvotoanki.core.domain.state.State;

public interface StateDao {

  List<State> getAll();

  long insert(String word);

  long batchInsert(List<State> state);

  long count();
}
