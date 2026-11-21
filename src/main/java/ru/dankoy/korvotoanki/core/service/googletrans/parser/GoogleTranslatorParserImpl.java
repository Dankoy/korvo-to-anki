package ru.dankoy.korvotoanki.core.service.googletrans.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.dankoy.korvotoanki.core.domain.googletranslation.Definition;
import ru.dankoy.korvotoanki.core.domain.googletranslation.GoogleTranslation;
import ru.dankoy.korvotoanki.core.domain.googletranslation.Synonym;
import ru.dankoy.korvotoanki.core.exceptions.GoogleTranslatorException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;

// Parses data for keys: t, at, md, rm, ss, ex, qca. Every section is optional and depends on
// requested dt options, so all of them are parsed defensively and could result in empty lists.

@Service
@RequiredArgsConstructor
public class GoogleTranslatorParserImpl implements GoogleTranslatorParser {

  private final JsonMapper mapper;

  @Override
  public GoogleTranslation parse(String data) {

    JsonNode jsonNodeRoot = toJsonNode(data);

    var googleTranslation = new GoogleTranslation(parseTranscription(jsonNodeRoot));

    googleTranslation.getTranslations().addAll(parseTranslations(jsonNodeRoot));
    googleTranslation.getDefinitions().addAll(parseDefinitions(jsonNodeRoot));
    googleTranslation.getSynonyms().addAll(parseSynonyms(jsonNodeRoot));
    googleTranslation.getExamples().addAll(parseExamples(jsonNodeRoot));
    googleTranslation.setPossibleMistake(parsePossibleMistake(jsonNodeRoot));

    return googleTranslation;
  }

  private String parseTranscription(JsonNode jsonNodeRoot) {

    // translation and transcription.
    JsonNode transcription = jsonNodeRoot.get(0) != null ? jsonNodeRoot.get(0).get(1) : null;
    if (Objects.nonNull(transcription)) {
      JsonNode transcriptionNode = transcription.get(3);
      return exists(transcriptionNode) ? transcriptionNode.asString() : null;
    }

    return null;
  }

  private List<String> parseTranslations(JsonNode jsonNodeRoot) {

    List<String> translationsStrings = new ArrayList<>();

    JsonNode multipleTranslations = jsonNodeRoot.get(5);

    // obtain list of translations
    if (exists(multipleTranslations)
        && exists(multipleTranslations.get(0))
        && exists(multipleTranslations.get(0).get(2))) {
      ArrayNode mts = (ArrayNode) multipleTranslations.get(0).get(2);
      translationsStrings =
          IntStream.range(0, mts.size())
              .mapToObj(mts::get)
              .map(c -> (ArrayNode) c)
              .map(n -> n.get(0).asString())
              .toList();
    }

    // remove url encoding from translations
    return translationsStrings.stream().map(t -> t.replaceAll("%20", " ")).toList();
  }

  private List<Definition> parseDefinitions(JsonNode jsonNodeRoot) {

    List<Definition> defs = new ArrayList<>();

    JsonNode definitions = jsonNodeRoot.get(12);

    // obtain definitions. Each element looks like:
    // ["noun", [["definition", "definition id", "example"], ...], "word"]
    if (exists(definitions)) {
      definitions.forEach(
          d -> {
            if (!exists(d) || !exists(d.get(0)) || !exists(d.get(1))) {
              return;
            }
            var type = d.get(0).asString();
            d.get(1)
                .forEach(
                    def -> {
                      if (!exists(def) || !exists(def.get(0))) {
                        return;
                      }
                      var info = def.get(0).asString();
                      // google translate lists category headers as definitions with blank text
                      if (info.isBlank()) {
                        return;
                      }
                      // definition id links the definition to synonyms of the same sense
                      var definitionId = exists(def.get(1)) ? def.get(1).asString() : null;
                      var example = exists(def.get(2)) ? def.get(2).asString() : null;
                      defs.add(new Definition(type, info, example, definitionId));
                    });
          });
    }

    return defs;
  }

  private List<Synonym> parseSynonyms(JsonNode jsonNodeRoot) {

    List<Synonym> synonyms = new ArrayList<>();

    JsonNode synonymsNode = jsonNodeRoot.get(11);

    // obtain synonyms. Each type element looks like:
    // ["noun", [[["synonym1", "synonym2"], "definition id", [["label"]]], ...], "word"]
    if (exists(synonymsNode)) {
      synonymsNode.forEach(
          s -> {
            if (!exists(s) || !exists(s.get(0)) || !exists(s.get(1))) {
              return;
            }
            var type = s.get(0).asString();
            s.get(1)
                .forEach(
                    group -> {
                      if (!exists(group) || !exists(group.get(0))) {
                        return;
                      }
                      List<String> words = new ArrayList<>();
                      if (group.get(0).isArray()) {
                        group.get(0).forEach(w -> words.add(w.asString()));
                      } else {
                        words.add(group.get(0).asString());
                      }
                      // definition id links the sense group to the definition, could be empty
                      var definitionId = exists(group.get(1)) ? group.get(1).asString() : null;
                      synonyms.add(new Synonym(type, definitionId, words));
                    });
          });
    }

    return synonyms;
  }

  private List<String> parseExamples(JsonNode jsonNodeRoot) {

    List<String> examples = new ArrayList<>();

    JsonNode examplesNode = jsonNodeRoot.get(13);

    // obtain examples. Real response wraps the list of examples into an outer list:
    // [13] = [[["example1",...],["example2",...]]]. The first element of the outer list is the
    // actual list of examples, where the first element of each example is the example text itself.
    // Example could contain html tags, they are stripped.
    if (exists(examplesNode)) {

      JsonNode first = examplesNode.get(0);
      boolean wrapped =
          exists(first) && first.isArray() && exists(first.get(0)) && first.get(0).isArray();
      JsonNode examplesList = wrapped ? first : examplesNode;

      examplesList.forEach(
          e -> {
            if (!exists(e) || !exists(e.get(0)) || !e.get(0).isString()) {
              return;
            }
            examples.add(e.get(0).asString().replaceAll("<[^>]*>", ""));
          });
    }

    return examples;
  }

  // obtain possible mistake suggestion. Expected to be a plain string, but could also be a list
  // of strings. Anything else is not a suggestion.
  private String parsePossibleMistake(JsonNode jsonNodeRoot) {

    JsonNode possibleMistakes = jsonNodeRoot.get(7);

    if (exists(possibleMistakes)) {
      if (possibleMistakes.isString()) {
        return possibleMistakes.asString();
      }
      if (possibleMistakes.isArray()) {
        for (JsonNode pm : possibleMistakes) {
          if (pm.isString()) {
            return pm.asString();
          }
        }
      }
    }

    return null;
  }

  private boolean exists(JsonNode node) {
    return Objects.nonNull(node) && !node.isNull();
  }

  private JsonNode toJsonNode(String json) {

    try {
      return mapper.readTree(json);
    } catch (Exception e) {
      throw new GoogleTranslatorException(String.format("Couldn't read json tree '%s'", json), e);
    }
  }
}
