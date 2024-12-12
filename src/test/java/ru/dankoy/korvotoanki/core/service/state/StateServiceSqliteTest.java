package ru.dankoy.korvotoanki.core.service.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dankoy.korvotoanki.core.dao.state.StateDao;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.domain.state.State;

@DisplayName("Testing of state service")
@ExtendWith(MockitoExtension.class)
class StateServiceSqliteTest {

  @Mock private StateDao stateDao;

  @InjectMocks private StateServiceSqlite stateService;

  /** Check states returned by service. */
  @Test
  @DisplayName("Check states returned by service")
  void testCheckState() {
    // Arrange
    List<State> states =
        Stream.of(
                new State(1L, "word1", LocalDateTime.now()),
                new State(2L, "word2", LocalDateTime.now()))
            .toList();
    when(stateDao.getAll()).thenReturn(states);

    // Act
    List<State> result = stateService.checkState();

    // Assert
    assertThat(result).isEqualTo(states);
  }

  /** Filter State with empty final state list returns all vocabularies. */
  @Test
  @DisplayName("Filter State with empty final state list returns all vocabularies")
  void testFilterState_EmptyFinalStateList_ReturnsAllVocabularies() {
    // Arrange
    List<Vocabulary> vocabularies = createVocabularyList();
    when(stateDao.getAll()).thenReturn(List.of());

    // Act
    List<Vocabulary> result = stateService.filterState(vocabularies);

    // Assert
    assertThat(result).isEqualTo(vocabularies);
  }

  /** Filter State with final state list containing vocabulary returns only missing vocabularies. */
  @Test
  @DisplayName(
      "Filter State with final state list containing vocabulary returns only missing vocabularies")
  void testFilterState_FinalStateListContainsVocabulary_ReturnsOnlyMissingVocabularies() {
    // Arrange
    List<Vocabulary> vocabularies = createVocabularyList();

    State state = new State(1L, "word1", LocalDateTime.now());
    when(stateDao.getAll()).thenReturn(List.of(state));

    // Act
    List<Vocabulary> result = stateService.filterState(vocabularies);

    // Assert
    assertThat(result).isEqualTo(Stream.of(getVocabularyByWord("word2")).toList());
  }

  /** Save States. */
  @Test
  @DisplayName("Save States")
  void testSaveState() {
    // Arrange
    List<Vocabulary> vocabularies = new ArrayList<>(createVocabularyList());

    when(stateDao.batchInsert(any())).thenReturn(new int[vocabularies.size()]);

    // Act
    stateService.saveState(vocabularies);

    // Assert
    assertThat(stateDao.getAll()).isNotNull();
  }

  /** Save State with transactional save operation. */
  @Test
  @DisplayName("Save State with transactional save operation")
  void testSaveState_Transactional() {
    // Arrange
    List<Vocabulary> vocabularies = createVocabularyList();

    when(stateDao.batchInsert(any()))
        .thenThrow(new RuntimeException("Error occurred in transaction"));

    // Act and Assert (Expecting exception)
    assertThatThrownBy(() -> stateService.saveState(vocabularies))
        .isInstanceOf(RuntimeException.class);
  }

  /**
   * Creates a list of vocabulary objects.
   *
   * @return A list of vocabulary objects
   */
  private List<Vocabulary> createVocabularyList() {

    var title = new Title(1L, "Title1", 1L);

    return Stream.of(
            new Vocabulary(
                "word1",
                title,
                1695239837,
                1695239837,
                1695240137,
                0,
                "combined forces.” He hoped to the gods it didn’t come to that.\n"
                    + "She fell silent, ",
                " a gratifying slaughter. Maybe even the final battle that would confirm her"
                    + " mastery. Most of all",
                0),
            new Vocabulary("word2", title, 1695239837, 1695239837, 1695240137, 0, null, null, 0))
        .toList();
  }

  /**
   * Retrieves a vocabulary object by its word.
   *
   * @param word The word to retrieve the vocabulary for
   * @return The vocabulary object or null if not found
   */
  private Vocabulary getVocabularyByWord(String word) {
    return createVocabularyList().stream()
        .filter(v -> v.word().equals(word))
        .findFirst()
        .orElse(null);
  }
}
