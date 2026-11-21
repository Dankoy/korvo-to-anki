package ru.dankoy.korvotoanki.core.service.title;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.dankoy.korvotoanki.core.dao.vocabularybuilder.title.TitleDaoJdbc;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.exceptions.KorvoRootException;

@DisplayName("Test TitleServiceJdbc ")
@SpringBootTest(classes = {TitleServiceJdbc.class, TitleDaoJdbc.class})
class TitleServiceJdbcTest {

  private static final long id = 1L;
  private static final String name = "title1";

  @MockitoBean private TitleDaoJdbc titleDaoJdbc;

  @Autowired private TitleServiceJdbc titleServiceJdbc;

  @Test
  void getAll() {

    List<Title> correct = correctTitles();

    given(titleDaoJdbc.getAll()).willReturn(correctTitles());

    List<Title> actual = titleServiceJdbc.getAll();

    assertThat(actual).isNotEmpty().isEqualTo(correct);

    Mockito.verify(titleDaoJdbc, times(1)).getAll();
  }

  @Test
  void getById() {

    List<Title> correct = correctTitles();

    given(titleDaoJdbc.getById(id)).willReturn(correctTitles().get(0));

    Title actual = titleServiceJdbc.getById(id);

    assertThat(actual).isEqualTo(correct.get(0));

    Mockito.verify(titleDaoJdbc, times(1)).getById(id);
  }

  @Test
  void getByName() {

    List<Title> correct = correctTitles();

    given(titleDaoJdbc.getByName(name)).willReturn(correctTitles().get(0));

    Title actual = titleServiceJdbc.getByName(name);

    assertThat(actual).isEqualTo(correct.get(0));

    Mockito.verify(titleDaoJdbc, times(1)).getByName(name);
  }

  @Test
  void getAllByNames() {

    var names = List.of("Title1", "Title2");
    var titles = List.of(new Title(1L, "Title1", 1L), new Title(2L, "Title2", 1L));

    given(titleDaoJdbc.getAllByNames(names)).willReturn(titles);

    List<Title> actual = titleServiceJdbc.getAllByNames(names);

    assertThat(actual).isEqualTo(titles);

    Mockito.verify(titleDaoJdbc, times(1)).getAllByNames(names);
  }

  @Test
  void getAllByNamesThrowsWhenBookNotFound() {

    var names = List.of("Title1", "Unknown");

    given(titleDaoJdbc.getAllByNames(names)).willReturn(List.of(new Title(1L, "Title1", 1L)));

    assertThatThrownBy(() -> titleServiceJdbc.getAllByNames(names))
        .isInstanceOf(KorvoRootException.class)
        .hasMessageContaining("Books not found in database: [Unknown]");

    Mockito.verify(titleDaoJdbc, times(1)).getAllByNames(names);
  }

  @Test
  void insert() {

    given(titleDaoJdbc.insert(name)).willReturn(id);

    var insertedId = titleServiceJdbc.insert(name);

    assertThat(insertedId).isEqualTo(id);

    Mockito.verify(titleDaoJdbc, times(1)).insert(name);
  }

  @Test
  void deleteById() {

    doNothing().when(titleDaoJdbc).deleteById(id);

    titleServiceJdbc.deleteById(id);

    Mockito.verify(titleDaoJdbc, times(1)).deleteById(id);
  }

  @Test
  void update() {

    var title = new Title(id, name, 1L);

    doNothing().when(titleDaoJdbc).update(title);

    titleServiceJdbc.update(title);

    Mockito.verify(titleDaoJdbc, times(1)).update(title);
  }

  @Test
  void count() {

    given(titleDaoJdbc.count()).willReturn((long) correctTitles().size());

    var actual = titleServiceJdbc.count();

    assertThat(actual).isEqualTo(correctTitles().size());

    Mockito.verify(titleDaoJdbc, times(1)).count();
  }

  private List<Title> correctTitles() {

    return Collections.singletonList(new Title(1L, "Title1", 1));
  }
}
