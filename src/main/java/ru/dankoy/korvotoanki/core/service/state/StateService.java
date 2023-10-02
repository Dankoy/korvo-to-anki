package ru.dankoy.korvotoanki.core.service.state;

import java.util.List;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.dto.State;

public interface StateService {

  List<State> checkState();

  List<Vocabulary> filterState(List<Vocabulary> vocabularies);

  void saveState(List<Vocabulary> vocabularies);
}
