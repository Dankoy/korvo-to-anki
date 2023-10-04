package ru.dankoy.korvotoanki.core.service.filenameformatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.dankoy.korvotoanki.core.service.datetimeprovider.DateTimeProvider;
import ru.dankoy.korvotoanki.core.service.datetimeprovider.DateTimeProviderImpl;


@SpringBootTest(classes = {FileNameFormatterServiceImpl.class, DateTimeProviderImpl.class})
@DisplayName("Test FileNameFormatterServiceImpl ")
class FileNameFormatterServiceImplTest {

  @MockBean
  private DateTimeProvider dateTimeProvider;

  @Autowired
  private FileNameFormatterServiceImpl fileNameFormatterService;

  @Test
  void format() {

    var fileName = "fileName";
    var correct = fileName + "-" + fileName + ".txt";

    given(dateTimeProvider.now()).willReturn(fileName);

    var actual = fileNameFormatterService.format(fileName);

    assertThat(actual).isEqualTo(correct);

  }
}