package ru.dankoy.korvotoanki.core.service.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.dankoy.korvotoanki.core.exceptions.IoException;
import ru.dankoy.korvotoanki.core.service.filenameformatter.FileNameFormatterService;
import ru.dankoy.korvotoanki.core.service.fileprovider.FileProviderService;

// Injection this bean from configuration class bean #{link IoServiceConfig}
@Primary
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

    if (fileName.endsWith("state")) {
      this.file = fileProviderService.provide(fileName);
    } else if (fileName.startsWith("export")) {
      this.file = fileProviderService.provide(fileNameFormatterService.format(fileName));
    } else {
      this.file = fileProviderService.provide(fileName);
    }
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
  public String readAllLines() throws IOException {

    if (Files.exists(file)) {
      log.info("Found file - {}", file);
      try {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        return String.join("", lines);
      } catch (IOException e) {
        throw new IoException(e);
      }
    } else {
      log.warn("File not found - {}", file);
      throw new IOException();
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
