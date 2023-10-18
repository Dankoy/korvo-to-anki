package ru.dankoy.korvotoanki.core.service.datetimeprovider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = {DateTimeProviderImpl.class})
@DisplayName("Test DateTimeProviderImpl ")
class DateTimeProviderImplTest {

  @Autowired
  private DateTimeProviderImpl dateTimeProvider;

  @Test
  void now() {

    var format = "yyyy-MM-dd_HH-mm-ss";
    var ld = LocalDate.of(1989, 1, 13);
    var lt = LocalTime.of(10, 10, 10);
    var ldt = LocalDateTime.of(ld, lt);

    var correct = ldt.format(DateTimeFormatter.ofPattern(format));

    try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
      mockedStatic.when(LocalDateTime::now).thenReturn(ldt);

      var actual = dateTimeProvider.now();

      assertThat(actual).isEqualTo(correct);

    }

  }


}