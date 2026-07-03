package ru.dankoy.korvotoanki.core.service.objectmapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.dankoy.korvotoanki.core.exceptions.ObjectMapperException;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class ObjectMapperServiceImpl implements ObjectMapperService {

  private final JsonMapper jsonMapper;

  @Override
  public String convertToString(Object object) {
    try {
      return jsonMapper.writeValueAsString(object);
    } catch (Exception e) {
      throw new ObjectMapperException(
          String.format("Couldn't convert object of type '%s' to string", object.getClass()), e);
    }
  }
}
