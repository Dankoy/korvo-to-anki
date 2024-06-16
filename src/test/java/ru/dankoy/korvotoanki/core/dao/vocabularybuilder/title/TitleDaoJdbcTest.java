package ru.dankoy.korvotoanki.core.dao.vocabularybuilder.title;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.exceptions.TitleDaoException;

@DisplayName("Test TitleDaoJdbc ")
@TestPropertySource(
    properties = {
      "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
    })
@JdbcTest
@Import(TitleDaoJdbc.class)
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE) // use embedded database
class TitleDaoJdbcTest {

  @Autowired private TitleDaoJdbc titleDaoJdbc;

  @DisplayName("getAll")
  @Test
  void getAll() {

    List<Title> correct = correctTitles();

    List<Title> titles = titleDaoJdbc.getAll();

    assertThat(titles).isNotEmpty().isEqualTo(correct);
  }

  @DisplayName("getById existing")
  @Test
  void getById() {

    var id = 1L;
    List<Title> correct = correctTitles();

    var expected = titleDaoJdbc.getById(id);

    assertThatCode(() -> titleDaoJdbc.getById(id)).doesNotThrowAnyException();

    assertThat(expected).isEqualTo(correct.get(0));
  }

  @DisplayName("getById non existing")
  @Test
  void getByIdNonExistent() {

    assertThatThrownBy(() -> titleDaoJdbc.getById(2L)).isInstanceOf(TitleDaoException.class);
  }

  @DisplayName("getByName existing")
  @Test
  void getByName() {

    var name = "Title1";
    List<Title> correct = correctTitles();

    var expected = titleDaoJdbc.getByName(name);

    assertThatCode(() -> titleDaoJdbc.getByName(name)).doesNotThrowAnyException();

    assertThat(expected).isEqualTo(correct.get(0));
  }

  @DisplayName("getByName non existing")
  @Test
  void getByNameNonExisting() {

    var name = "Title2";

    assertThatThrownBy(() -> titleDaoJdbc.getByName(name)).isInstanceOf(TitleDaoException.class);
  }

  @DisplayName("insert")
  @Test
  void insert() {

    var name = "Title2";

    var id = titleDaoJdbc.insert(name);

    var title = new Title(id, name, 1L);

    var actual = titleDaoJdbc.getById(id);

    assertThat(actual).isEqualTo(title);
  }

  @DisplayName("deleteById existing")
  @Test
  void deleteById() {

    var id = 1L;

    assertThatCode(() -> titleDaoJdbc.getById(id)).doesNotThrowAnyException();

    titleDaoJdbc.deleteById(id);

    assertThatThrownBy(() -> titleDaoJdbc.getById(id)).isInstanceOf(TitleDaoException.class);
  }

  @DisplayName("deleteById non existing")
  @Test
  void deleteByIdNonExisting() {

    var id = 2L;

    assertThatCode(() -> titleDaoJdbc.deleteById(id)).doesNotThrowAnyException();
  }

  @DisplayName("update existing")
  @Test
  void update() {

    var id = 1L;

    var toUpdate = new Title(id, "updated", 1L);

    titleDaoJdbc.update(toUpdate);

    var actual = titleDaoJdbc.getById(id);

    assertThat(actual).isEqualTo(toUpdate);
  }

  @DisplayName("update non existing")
  @Test
  void updateNonExisting() {

    var id = 2L;

    var toUpdate = new Title(id, "updated", 1L);

    assertThatCode(() -> titleDaoJdbc.update(toUpdate)).doesNotThrowAnyException();
  }

  @Test
  void count() {

    var correct = correctTitles().size();

    var actual = titleDaoJdbc.count();

    assertThat(actual).isEqualTo(correct);
  }

  private List<Title> correctTitles() {

    return Collections.singletonList(new Title(1L, "Title1", 1));
  }
}
