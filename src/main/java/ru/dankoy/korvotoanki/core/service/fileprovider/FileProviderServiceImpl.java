package ru.dankoy.korvotoanki.core.service.fileprovider;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@NoArgsConstructor
public class FileProviderServiceImpl implements FileProviderService {

  @Override
  public Path provide(String name) {

    String pathToDir = System.getProperty("user.dir");

    String path = pathToDir + File.separator + name;

    return Paths.get(path);
  }
}
