package ru.dankoy.korvotoanki.core.service.state;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;
import ru.dankoy.korvotoanki.config.appprops.FilesProperties;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.dto.State;
import ru.dankoy.korvotoanki.core.service.filenameformatter.FileNameFormatterService;
import ru.dankoy.korvotoanki.core.service.fileprovider.FileProviderService;
import ru.dankoy.korvotoanki.core.service.io.IOService;
import ru.dankoy.korvotoanki.core.service.objectmapper.ObjectMapperService;


@Slf4j
@Component
@RequiredArgsConstructor
public class StateServiceImpl implements StateService {

  private final FilesProperties filesProperties;
  private final ObjectMapper mapper;
  private final ObjectMapperService mapperService;

  // The IoService is provided type, that's why we inject it using @Lookup annotation.
  // @Lookup annotation doesn't work inside prototype bean, so had to use constructor to inject beans
  @Lookup
  public IOService getIoService(FileProviderService fileProviderService,
      FileNameFormatterService fileNameFormatterService, String fileName) {
    return null;
  }

  @Lookup
  public FileProviderService getFileProviderService() {
    return null;
  }

  @Lookup
  public FileNameFormatterService getFileNameFormatterService() {
    return null;
  }

  @Override
  public List<State> checkState() {

    var ioServiceState = getIoService(
        getFileProviderService(),
        getFileNameFormatterService(),
        filesProperties.getStateFileName());

    List<State> stateList = new ArrayList<>();

    try {
      var state = ioServiceState.readAllLines();
      stateList = mapper.readValue(state, new TypeReference<List<State>>() {
      });
    } catch (IOException e) {
      log.warn("Unable to read state from file");
    }

    return stateList;
  }

  @Override
  public List<Vocabulary> filterState(List<Vocabulary> vocabularies) {

    final List<State> finalStateList = checkState();
    if (!finalStateList.isEmpty()) {
      return vocabularies.stream()
          .filter(v -> finalStateList.stream()
              .noneMatch(s -> s.getWord().equals(v.word()))
          )
          .toList();
    }
    return vocabularies;
  }

  @Override
  public void saveState(List<Vocabulary> vocabularies) {

    // add new exported data to existing state

    var ioServiceState = getIoService(
        getFileProviderService(),
        getFileNameFormatterService(),
        filesProperties.getStateFileName());

    final List<State> currentState = checkState();

    List<State> states = vocabularies.stream()
        .map(v -> new State(v.word()))
        .toList();

    currentState.addAll(states);

    var string = mapperService.convertToString(currentState);

    ioServiceState.print(string);

  }


}
