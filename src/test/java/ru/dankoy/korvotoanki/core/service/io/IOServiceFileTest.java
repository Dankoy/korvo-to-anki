package ru.dankoy.korvotoanki.core.service.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.dankoy.korvotoanki.core.service.datetimeprovider.DateTimeProviderImpl;
import ru.dankoy.korvotoanki.core.service.filenameformatter.FileNameFormatterService;
import ru.dankoy.korvotoanki.core.service.filenameformatter.FileNameFormatterServiceImpl;
import ru.dankoy.korvotoanki.core.service.fileprovider.FileProviderService;
import ru.dankoy.korvotoanki.core.service.fileprovider.FileProviderServiceImpl;

@SpringBootTest(
    classes = {
      FileNameFormatterServiceImpl.class,
      FileProviderServiceImpl.class,
      DateTimeProviderImpl.class
    })
class IOServiceFileTest {

  @MockitoBean private FileProviderService fileProviderService;

  @Autowired private FileNameFormatterService fileNameFormatterService;

  @TempDir private Path tempDir;

  private final String fileName = "test.txt";

  @Test
  void testPrint() throws Exception {
    // Arrange
    String message = "Hello, World!";

    given(fileProviderService.provide(fileName)).willReturn(tempDir.resolve(fileName));

    IOServiceFile ioServiceFile =
        new IOServiceFile(fileProviderService, fileNameFormatterService, fileName);

    // Act
    ioServiceFile.print(message);

    // Assert
    assertFilesEqual(tempDir.resolve(fileName), message);
  }

  @Test
  void testReadAllLines() throws Exception {
    // Arrange
    String content =
        """
        Line 1
        Line 2
        Line 3
        """;
    Files.write(tempDir.resolve(fileName), content.getBytes());

    given(fileProviderService.provide(fileName)).willReturn(tempDir.resolve(fileName));

    IOServiceFile ioServiceFile =
        new IOServiceFile(fileProviderService, fileNameFormatterService, fileName);

    // Act
    String result = ioServiceFile.readAllLines();

    // Assert
    assertFilesEqual(tempDir.resolve(fileName), result);
  }

  @Test
  void testReadLn() {
    // Arrange
    IOServiceFile ioServiceFile =
        new IOServiceFile(fileProviderService, fileNameFormatterService, fileName);

    // Act and Assert
    assertThrows(UnsupportedOperationException.class, () -> ioServiceFile.readLn());
  }

  @Test
  void testReadLong() {
    // Arrange
    IOServiceFile ioServiceFile =
        new IOServiceFile(fileProviderService, fileNameFormatterService, fileName);

    // Act and Assert
    assertThrows(UnsupportedOperationException.class, () -> ioServiceFile.readLong());
  }

  private void assertFilesEqual(Path file1, String content) throws Exception {

    List<String> lines = Files.readAllLines(file1, StandardCharsets.UTF_8);
    var result = String.join("", lines);

    assertEquals(content, result);
  }
}
