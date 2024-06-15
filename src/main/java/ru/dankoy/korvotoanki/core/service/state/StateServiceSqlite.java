package ru.dankoy.korvotoanki.core.service.state;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ru.dankoy.korvotoanki.core.dao.state.StateDao;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.domain.state.State;

@RequiredArgsConstructor
public class StateServiceSqlite implements StateService {

  private final StateDao stateDao;

  @Override
  public List<State> checkState() {
    return stateDao.getAll();
  }

  @Override
  public List<Vocabulary> filterState(List<Vocabulary> vocabularies) {
    return List.of();
  }

  @Override
  public void saveState(List<Vocabulary> vocabularies) {

    List<State> states =
        vocabularies.stream().map(v -> new State(0L, v.word(), LocalDateTime.now())).toList();

    stateDao.batchInsert(states);
  }
}
