package ru.dankoy.korvotoanki.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.dankoy.korvotoanki.core.service.io.IOService;
import ru.dankoy.korvotoanki.core.service.io.IOServiceFile;

@Configuration
public class IoServiceConfig {

  // default file name to export to
  @Value("${spring.application.name}.txt")
  private String fileName;

  @Bean
  public IOService ioService() {
    return new IOServiceFile(fileName, fileName);
  }


}
