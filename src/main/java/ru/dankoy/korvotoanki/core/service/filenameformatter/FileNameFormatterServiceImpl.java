package ru.dankoy.korvotoanki.core.service.filenameformatter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dankoy.korvotoanki.core.service.datetimeprovider.DateTimeProvider;

@Component
@RequiredArgsConstructor
public class FileNameFormatterServiceImpl implements FileNameFormatterService {

  private final DateTimeProvider dateTimeProvider;

  @Override
  public String format(String fileName) {

    return fileName + "-" + dateTimeProvider.now() + ".txt";
  }
}
