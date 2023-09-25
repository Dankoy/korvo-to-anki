package ru.dankoy.korvotoanki.core.service.googletrans.parser;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.dankoy.korvotoanki.core.domain.googletranslation.Definition;
import ru.dankoy.korvotoanki.core.domain.googletranslation.GoogleTranslation;
import ru.dankoy.korvotoanki.core.exceptions.GoogleTranslatorException;

// Currently parse only data for keys: t,at,md,rm

@Service
@RequiredArgsConstructor
public class GoogleTranslatorParserImpl implements GoogleTranslatorParser {

  private final ObjectMapper mapper;

  @Override
  public GoogleTranslation parse(String data) {

    String transcriptionString = null;

    JsonNode jsonNodeRoot = toJsonNode(data);

    // translation and transcription.
    ArrayNode translationAndTranscriptionNode = (ArrayNode) jsonNodeRoot.get(0);
    ArrayNode multipleTranslations = (ArrayNode) jsonNodeRoot.get(5);

    ArrayNode definitions = (ArrayNode) jsonNodeRoot.get(12);

    // obtain transcription
    JsonNode transcription = translationAndTranscriptionNode.get(1).get(3);
    if (Objects.nonNull(transcription)) {
      transcriptionString = transcription.asText();
    }

    // obtain list of translations
    ArrayNode mts = (ArrayNode) multipleTranslations.get(0).get(2);
    List<String> translations = IntStream.range(0, mts.size())
        .mapToObj(mts::get)
        .map(c -> (ArrayNode) c)
        .map(n -> n.get(0).asText())
        .toList();

    List<Definition> defs = new ArrayList<>();
    // obtain definitions
    if (Objects.nonNull(definitions)) {
      defs = IntStream.range(0, definitions.size())
          .mapToObj(definitions::get)
          .map(d -> {
            var type = d.get(0).asText();
            var def = d.get(1).get(0).get(0).asText();
            return new Definition(type, def);
          })
          .toList();
    }

    var googleTranslation = new GoogleTranslation(transcriptionString);
    googleTranslation.getTranslations().addAll(translations);
    googleTranslation.getDefinitions().addAll(defs);

    return googleTranslation;

  }

  private JsonNode toJsonNode(String json) {

    try {
      return mapper.readTree(json);
    } catch (Exception e) {
      throw new GoogleTranslatorException(
          String.format("Couldn't read json tree '%s'", json), e);
    }

  }


}
