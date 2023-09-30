package ru.dankoy.korvotoanki.core.service.io;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.dankoy.korvotoanki.core.exceptions.IoException;

@Slf4j
public class IOServiceFile implements IOService {

  private final Path fileOutput;
  private final Path fileInput;

  public IOServiceFile(String fileName, String fileInput) {

    URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
    String path = url.getPath() + File.separator + fileName;

    this.fileOutput = Paths.get(path);
    this.fileInput = Paths.get(fileInput);
  }

  @Override
  public void print(String message) {
    try {
      Files.writeString(fileOutput, message);
      log.info("Saved file as - {}", fileOutput.toFile());
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
