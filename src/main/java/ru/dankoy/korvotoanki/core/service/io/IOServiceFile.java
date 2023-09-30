package ru.dankoy.korvotoanki.core.service.io;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import ru.dankoy.korvotoanki.core.exceptions.IoException;

@Slf4j
public class IOServiceFile implements IOService {

  private final Path fileOutput;
  private final Path fileInput;

  public IOServiceFile(String fileOut, String fileInput) {

    String pathToDir = System.getProperty("user.dir");

    String pathOutput = pathToDir + File.separator + fileOut;
    String pathInput = pathToDir + File.separator + fileInput;

    this.fileOutput = Paths.get(pathOutput);
    this.fileInput = Paths.get(pathInput);
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
