package ru.dankoy.korvotoanki.config.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import ru.dankoy.korvotoanki.config.appprops.FilesProperties;
import ru.dankoy.korvotoanki.core.service.objectmapper.ObjectMapperService;
import ru.dankoy.korvotoanki.core.service.state.StateService;
import ru.dankoy.korvotoanki.core.service.state.StateServiceImpl;

@RequiredArgsConstructor
// @Configuration
public class StateConfig {

  private final FilesProperties filesProperties;
  private final ObjectMapper objectMapper;
  private final ObjectMapperService objectMapperService;

  @Bean(name = "fileStateService")
  public StateService fileStateService() {
    return new StateServiceImpl(filesProperties, objectMapper, objectMapperService);
  }

  @Bean(name = "sqliteStateService")
  public StateService sqliteStateService() {
    return new StateServiceImpl(filesProperties, objectMapper, objectMapperService);
  }
}
