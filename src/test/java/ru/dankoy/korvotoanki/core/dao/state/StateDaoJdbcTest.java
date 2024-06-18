package ru.dankoy.korvotoanki.core.dao.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.test.context.TestPropertySource;
import ru.dankoy.korvotoanki.config.datasource.StateDataSourceConfig;
import ru.dankoy.korvotoanki.core.domain.state.State;
import ru.dankoy.korvotoanki.core.exceptions.StateDaoException;

@DisplayName("Test StateDaoJdbc ")
@Import({StateDataSourceConfig.class, StateDaoJdbc.class})
@TestPropertySource(
    properties = {
      "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
          + " org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration"
    })
@JdbcTest
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE) // use embedded database
class StateDaoJdbcTest {

  @Autowired private StateDaoJdbc stateDao;

  @Autowired private NamedParameterJdbcOperations stateJdbcOperations;

  @DisplayName("getAll expects correct response")
  @Test
  void getAllTest() {

    List<State> stateList = stateDao.getAll();

    assertThat(stateList).isNotNull().isNotEmpty().isEqualTo(makeState());
  }

  @DisplayName("insert expects correct insertion")
  @Test
  void insertTest() {

    var word = "word3";
    var toInsert = new State(3, word, LocalDateTime.now());

    var id = stateDao.insert(word);

    var jdbcOperations = stateJdbcOperations.getJdbcOperations();

    // check only for word string, not more.
    var actualWord =
        jdbcOperations.queryForObject("select word from state where id = ?", String.class, id);

    assertThat(actualWord).isNotNull().isEqualTo(toInsert.word());
  }

  @DisplayName("batchInsert test expects correct insertion")
  @Test
  void batchInsertTest() {

    var toInsert = makeStateToBatchInsert();

    var amountOfRowsUpdated = stateDao.batchInsert(toInsert);

    assertThat(amountOfRowsUpdated).hasSize(toInsert.size());
  }

  @DisplayName("batchInsert test expects not null constraint exception")
  @Test
  void batchInsertTest_ExpectException() {

    var toInsert = makeStateToBatchInsertWithNullWord();

    assertThatThrownBy(() -> stateDao.batchInsert(toInsert)).isInstanceOf(StateDaoException.class);
  }

  @DisplayName("count expects correct response")
  @Test
  void countTest() {

    var count = stateDao.count();

    assertThat(count).isPositive().isEqualTo(makeState().size());
  }

  private List<State> makeState() {

    var date = LocalDateTime.of(LocalDate.of(2024, 1, 1), LocalTime.of(1, 1));

    List<State> stateList = new ArrayList<>();
    stateList.add(new State(1L, "word1", date));
    stateList.add(new State(2L, "word2", date));
    return stateList;
  }

  private List<State> makeStateToBatchInsert() {

    List<State> stateList = new ArrayList<>();
    var date = LocalDateTime.of(LocalDate.of(2024, 1, 1), LocalTime.of(1, 1));

    for (int i = 3; i < 120; i++) {

      stateList.add(new State(0, "word" + i, date));
    }
    return stateList;
  }

  private List<State> makeStateToBatchInsertWithNullWord() {

    List<State> stateList = new ArrayList<>();
    var date = LocalDateTime.of(LocalDate.of(2024, 1, 1), LocalTime.of(1, 1));

    for (int i = 3; i < 120; i++) {

      stateList.add(new State(0, null, date));
    }
    return stateList;
  }
}
