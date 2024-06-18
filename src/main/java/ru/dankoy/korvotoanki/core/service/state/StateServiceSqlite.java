package ru.dankoy.korvotoanki.core.service.state;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dankoy.korvotoanki.core.dao.state.StateDao;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.domain.state.State;

@Slf4j
@RequiredArgsConstructor
@Service("sqliteStateService")
public class StateServiceSqlite implements StateService {

  private final StateDao stateDao;

  @Override
  public List<State> checkState() {
    return stateDao.getAll();
  }

  @Override
  public List<Vocabulary> filterState(List<Vocabulary> vocabularies) {

    final List<State> finalStateList = checkState();
    if (!finalStateList.isEmpty()) {
      return vocabularies.stream()
          .filter(v -> finalStateList.stream().noneMatch(s -> s.word().equals(v.word())))
          .toList();
    }
    return vocabularies;
  }

  @Transactional("stateTransactionManager")
  @Override
  public void saveState(List<Vocabulary> vocabularies) {

    List<State> states =
        vocabularies.stream().map(v -> new State(0L, v.word(), LocalDateTime.now())).toList();

    stateDao.batchInsert(states);

    log.info("Saved state");
  }
}
