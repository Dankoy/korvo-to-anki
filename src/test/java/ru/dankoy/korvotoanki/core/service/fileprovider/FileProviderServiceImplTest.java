package ru.dankoy.korvotoanki.core.service.fileprovider;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Paths;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = {FileProviderServiceImpl.class})
@DisplayName("Test FileProviderServiceImpl ")
class FileProviderServiceImplTest {

  @Autowired
  private FileProviderService fileProviderService;

  @Test
  void provide() {

    //todo: mock
    var fileName = "fileName";
    var currentDir = System.getProperty("user.dir");
    var path = Paths.get(currentDir + File.separator + fileName);

    var actual = fileProviderService.provide(fileName);

    assertThat(actual).isEqualTo(path);

  }
}