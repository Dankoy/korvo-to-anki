package ru.dankoy.korvotoanki.config;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import ru.dankoy.korvotoanki.core.service.filenameformatter.FileNameFormatterService;
import ru.dankoy.korvotoanki.core.service.fileprovider.FileProviderService;
import ru.dankoy.korvotoanki.core.service.io.IOService;
import ru.dankoy.korvotoanki.core.service.io.IOServiceFile;

@RequiredArgsConstructor
@Configuration
public class IoServiceConfig {

  private final FileProviderService fileProviderService;
  private final FileNameFormatterService fileNameFormatterService;

  @Bean
  public Function<String, IOService> ioServiceFileFactory() {
    return name -> ioServiceFile(fileProviderService, fileNameFormatterService, name);
  }

  /** This bean is created as prototype and here only to inject it by function bean. */
  @Bean
  @Scope("prototype")
  public IOService ioServiceFile(
      FileProviderService fileProviderService,
      FileNameFormatterService fileNameFormatterService,
      String name) {
    return new IOServiceFile(fileProviderService, fileNameFormatterService, name);
  }

  @FunctionalInterface
  public interface IoServiceFactory {

    IOService ioServiceFileCreate(
        FileProviderService fileProviderService,
        FileNameFormatterService fileNameFormatterService,
        String name);
  }
}
