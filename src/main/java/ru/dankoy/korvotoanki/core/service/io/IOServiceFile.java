package ru.dankoy.korvotoanki.core.service.io;

import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.dankoy.korvotoanki.core.exceptions.IoException;
import ru.dankoy.korvotoanki.core.service.filenameformatter.FileNameFormatterService;
import ru.dankoy.korvotoanki.core.service.fileprovider.FileProviderService;

@Service
@Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
@Slf4j
public class IOServiceFile implements IOService {

  private final Path file;

  public IOServiceFile(
      FileProviderService fileProviderService,
      FileNameFormatterService fileNameFormatterService,
      String fileName) {

    //  Lookup doesn't work in prototype beans.
    //  Had to move these methods in client code of exporter service

    //    var fileProviderService = fileProviderServiceBeanFactory.get();
    //    var fileNameFormatterService = fileNameFormatterServiceBeanFactory.get();

    this.file = fileProviderService.provide(fileNameFormatterService.format(fileName));
  }

  @Override
  public void print(String message) {
    try {
      Files.writeString(file, message);
      log.info("Saved file as - {}", file.toFile());
    } catch (Exception e) {
      throw new IoException(e);
    }
  }

  @Override
  public String readLn() {
    throw new UnsupportedOperationException();
  }

  @Override
  public long readLong() {
    throw new UnsupportedOperationException();
  }

}
