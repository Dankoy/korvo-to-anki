package ru.dankoy.korvotoanki.core.dao.vocabularybuilder.vocabulary;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.test.context.TestPropertySource;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;

@DisplayName("Test VocabularyDaoJdbc ")
@TestPropertySource(
    properties = {
      "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
    })
@JdbcTest
@Import(VocabularyDaoJdbc.class)
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE) // use embedded database
class VocabularyDaoJdbcTest {

  @Autowired private VocabularyDaoJdbc vocabularyDaoJdbc;

  @Autowired private JdbcOperations jdbcOperations;

  @DisplayName("getAll return non empty")
  @Test
  void getAll() {

    List<Vocabulary> correct = correctVocabularies();

    List<Vocabulary> actual = vocabularyDaoJdbc.getAll();

    assertThat(actual).isNotEmpty().isEqualTo(correct);
  }

  @DisplayName("getAll return empty list")
  @Test
  void getAllEmpty() {

    jdbcOperations.execute("delete from vocabulary");
    List<Vocabulary> actual = vocabularyDaoJdbc.getAll();

    assertThat(actual).isEmpty();
  }

  @DisplayName("getByTitle return found")
  @Test
  void getByTitle() {

    List<Vocabulary> correct = correctVocabularies();

    var title = new Title(1L, "Title1", 1L);

    List<Vocabulary> actual = vocabularyDaoJdbc.getByTitle(title);

    assertThat(actual).isNotEmpty().isEqualTo(correct);
  }

  @DisplayName("getByTitle return empty list")
  @Test
  void getByTitleReturnEmptyList() {

    var title = new Title(2L, "Title2", 1L);

    List<Vocabulary> actual = vocabularyDaoJdbc.getByTitle(title);

    assertThat(actual).isEmpty();
  }

  @DisplayName("count return non empty")
  @Test
  void count() {

    var correct = correctVocabularies().size();

    var actual = vocabularyDaoJdbc.count();

    assertThat(actual).isEqualTo(correct);
  }

  @DisplayName("count return 0")
  @Test
  void countZero() {

    jdbcOperations.execute("delete from vocabulary");
    var actual = vocabularyDaoJdbc.count();

    assertThat(actual).isZero();
  }

  private List<Vocabulary> correctVocabularies() {

    var title = new Title(1L, "Title1", 1L);

    return Stream.of(
            new Vocabulary(
                "contemplating",
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
            new Vocabulary("word", title, 1695239837, 1695239837, 1695240137, 0, null, null, 0))
        .toList();
  }
}
