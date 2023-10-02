package ru.dankoy.korvotoanki.core.service.filenameformatter;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;
import ru.dankoy.korvotoanki.core.service.datetimeprovider.DateTimeProvider;

@Component
@NoArgsConstructor
public class FileNameFormatterServiceImpl implements FileNameFormatterService {

  // Prototype, creates every time
  @Lookup
  public DateTimeProvider getDateTimeProvider() {
    return null;
  }

  @Override
  public String format(String fileName) {

    var dateTimeProvider = getDateTimeProvider();

    return fileName + "-" + dateTimeProvider.now() + ".txt";
  }
}
