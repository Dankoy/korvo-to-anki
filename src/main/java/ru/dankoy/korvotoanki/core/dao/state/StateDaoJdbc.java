package ru.dankoy.korvotoanki.core.dao.state;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.dankoy.korvotoanki.core.domain.state.State;
import ru.dankoy.korvotoanki.core.exceptions.StateDaoException;

@RequiredArgsConstructor
@Component
public class StateDaoJdbc implements StateDao {

  private final NamedParameterJdbcOperations stateJdbcOperations;

  @Override
  public List<State> getAll() {
    return stateJdbcOperations.query("select id, word, created from state", new StateMapper());
  }

  @Override
  public long insert(String word) {
    var now = LocalDateTime.now();

    MapSqlParameterSource parameters =
        new MapSqlParameterSource().addValue("word", word).addValue("created", now);
    KeyHolder keyHolder = new GeneratedKeyHolder();
    stateJdbcOperations.update(
        "insert into state (word, created) values (:word, :created)", parameters, keyHolder);

    try {
      return Objects.requireNonNull(keyHolder.getKey()).longValue();
    } catch (Exception e) {
      throw new StateDaoException(e);
    }
  }

  @Override
  public int[] batchInsert(List<State> state) {

    SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(state.toArray());

    KeyHolder keyHolder = new GeneratedKeyHolder();

    int[] rowsUpdated;

    try {
      rowsUpdated =
          stateJdbcOperations.batchUpdate(
              "insert into state (word, created) values (:word, :created)", batch, keyHolder);

    } catch (Exception ex) {
      throw new StateDaoException(ex);
    }

    var eq = Objects.equals(batch.length, rowsUpdated.length);

    if (eq) {
      return rowsUpdated;
    } else {
      throw new StateDaoException("Batch insert error");
    }
  }

  @Override
  public long count() {
    Long count =
        stateJdbcOperations
            .getJdbcOperations()
            .queryForObject("select count(*) from state", Long.class);
    return count == null ? 0 : count;
  }

  private static class StateMapper implements RowMapper<State> {

    @Override
    public State mapRow(ResultSet resultSet, int i) throws SQLException {
      long id = resultSet.getLong("id");
      String name = resultSet.getString("word");
      LocalDateTime createdAt = resultSet.getObject("created", LocalDateTime.class);
      return new State(id, name, createdAt);
    }
  }
}
