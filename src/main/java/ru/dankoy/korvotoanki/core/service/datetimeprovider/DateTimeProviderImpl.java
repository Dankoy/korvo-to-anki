package ru.dankoy.korvotoanki.core.service.datetimeprovider;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class DateTimeProviderImpl implements DateTimeProvider {

  @Override
  public String now() {

    var format = "yyyy-MM-dd_HH:mm:ss";

    var localDateTime = LocalDateTime.now();

    return localDateTime.format(DateTimeFormatter.ofPattern(format));
  }
}
