package ru.dankoy.korvotoanki.core.service.datetimeprovider;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DateTimeProviderImpl implements DateTimeProvider {

  @Override
  public String now() {

    var format = "yyyy-MM-dd_HH-mm-ss";

    var localDateTime = LocalDateTime.now();

    return localDateTime.format(DateTimeFormatter.ofPattern(format));
  }
}
