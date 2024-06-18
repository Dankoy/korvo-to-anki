package ru.dankoy.korvotoanki.core.dao.vocabularybuilder.title;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.exceptions.TitleDaoException;

@RequiredArgsConstructor
@Component
public class TitleDaoJdbc implements TitleDao {

  private final NamedParameterJdbcOperations vocabularyJdbcOperations;

  @Override
  public List<Title> getAll() {
    return vocabularyJdbcOperations.query("select id, name, filter from title", new TitleMapper());
  }

  @Override
  public Title getById(long id) {
    Map<String, Object> params = Collections.singletonMap("id", id);
    try {
      return vocabularyJdbcOperations.queryForObject(
          "select id, name, filter from title where id = :id", params, new TitleMapper());
    } catch (Exception e) {
      throw new TitleDaoException(e);
    }
  }

  @Override
  public Title getByName(String name) {
    Map<String, Object> params = Collections.singletonMap("name", name);
    try {
      return vocabularyJdbcOperations.queryForObject(
          "select id, name, filter from title where name = :name", params, new TitleMapper());
    } catch (Exception e) {
      throw new TitleDaoException(e);
    }
  }

  @Override
  public long insert(String titleName) {
    MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("name", titleName);
    KeyHolder keyHolder = new GeneratedKeyHolder();
    vocabularyJdbcOperations.update(
        "insert into title (name) values (:name)", parameters, keyHolder);

    try {
      return Objects.requireNonNull(keyHolder.getKey()).longValue();
    } catch (Exception e) {
      throw new TitleDaoException(e);
    }
  }

  @Override
  public void deleteById(long id) {
    Map<String, Object> params = Collections.singletonMap("id", id);
    vocabularyJdbcOperations.update("delete from title where id = :id", params);
  }

  @Override
  public void update(Title title) {
    vocabularyJdbcOperations.update(
        "update title set name = :name where id = :id",
        Map.of("id", title.id(), "name", title.name()));
  }

  @Override
  public long count() {
    Long count =
        vocabularyJdbcOperations
            .getJdbcOperations()
            .queryForObject("select count(*) from title", Long.class);
    return count == null ? 0 : count;
  }

  private static class TitleMapper implements RowMapper<Title> {

    @Override
    public Title mapRow(ResultSet resultSet, int i) throws SQLException {
      long id = resultSet.getLong("id");
      String name = resultSet.getString("name");
      long filter = resultSet.getLong("filter");
      return new Title(id, name, filter);
    }
  }
}
