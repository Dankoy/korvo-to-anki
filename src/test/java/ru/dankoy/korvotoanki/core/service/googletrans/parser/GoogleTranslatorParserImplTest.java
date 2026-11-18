package ru.dankoy.korvotoanki.core.service.googletrans.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.dankoy.korvotoanki.core.domain.googletranslation.Definition;
import ru.dankoy.korvotoanki.core.domain.googletranslation.Synonym;
import ru.dankoy.korvotoanki.core.exceptions.GoogleTranslatorException;
import tools.jackson.databind.json.JsonMapper;

@DisplayName("Test GoogleTranslatorParserImpl ")
class GoogleTranslatorParserImplTest {

  private GoogleTranslatorParserImpl parser;

  @BeforeEach
  void setUp() {
    parser = new GoogleTranslatorParserImpl(new JsonMapper());
  }

  @DisplayName("parse full response with all dt options")
  @Test
  void parseFullResponse() {

    var result = parser.parse(fullResponse());

    assertThat(result.getTranscription()).isEqualTo("həˈləʊ");
    assertThat(result.getTranslations()).containsExactly("мелочь", "не мелочь", "пустяк");
    assertThat(result.getPossibleMistake()).isEqualTo("hallo");
    assertThat(result.getDefinitions())
        .containsExactly(
            new Definition(
                "exclamation",
                "an expression of greeting",
                "every morning they exchanged polite hellos",
                "123.0"),
            new Definition("exclamation", "used to begin a phone conversation", null, "123.1"),
            new Definition(
                "noun", "a greeting or reception", "the proposal got a warm welcome", "124.0"));
    assertThat(result.getSynonyms())
        .containsExactly(new Synonym("noun", "noun.1", List.of("приветствие", "салют")));
    assertThat(result.getExamples())
        .containsExactly("La fourchette des prix.", "Another example sentence.");
  }

  @DisplayName("parse response without new dt options results in empty optional sections")
  @Test
  void parseResponseWithoutNewOptions() {

    var result = parser.parse("[[[\"привет\",\"hello\",null,null,10]],null,\"en\"]");

    assertThat(result.getTranscription()).isNull();
    assertThat(result.getTranslations()).isEmpty();
    assertThat(result.getDefinitions()).isEmpty();
    assertThat(result.getSynonyms()).isEmpty();
    assertThat(result.getExamples()).isEmpty();
    assertThat(result.getPossibleMistake()).isNull();
  }

  @DisplayName("parse response with null sections results in empty optional sections")
  @Test
  void parseResponseWithNullSections() {

    var result =
        parser.parse(
            """
            [
              [["привет","hello",null,null,10]],
              null,
              "en",
              null,
              null,
              [["привет",null,[["мелочь",0,true,false]]]],
              0.9,
              null,
              [["en"]],
              null,
              null,
              null,
              null,
              null,
              null
            ]
            """);

    assertThat(result.getTranscription()).isNull();
    assertThat(result.getTranslations()).containsExactly("мелочь");
    assertThat(result.getDefinitions()).isEmpty();
    assertThat(result.getSynonyms()).isEmpty();
    assertThat(result.getExamples()).isEmpty();
    assertThat(result.getPossibleMistake()).isNull();
  }

  @DisplayName("parse response with partially malformed sections")
  @Test
  void parseResponseWithMalformedSections() {

    var result =
        parser.parse(
            """
            [
              [["привет","hello",null,null,10]],
              null,
              "en",
              null,
              null,
              null,
              null,
              42,
              null,
              null,
              null,
              [["noun",[[["приветствие"],""]],"hello"],[null,[[["салют"],""]],"hello"]],
              [["exclamation",null,"hello"],["noun",[["a greeting",null],null],"hello"]],
              [[null]]
            ]
            """);

    // sections with valid entries are parsed, malformed entries are skipped
    assertThat(result.getSynonyms())
        .containsExactly(new Synonym("noun", "", List.of("приветствие")));
    assertThat(result.getDefinitions())
        .containsExactly(new Definition("noun", "a greeting", null, null));
    assertThat(result.getExamples()).isEmpty();
    assertThat(result.getPossibleMistake()).isNull();
  }

  @DisplayName("parse possible mistake as list of strings")
  @Test
  void parsePossibleMistakeAsList() {

    var result =
        parser.parse(
            "[[[\"helo\",\"helo\",null,null,10]],null,\"en\",null,null,null,null,[\"hello\"]]");

    assertThat(result.getPossibleMistake()).isEqualTo("hello");
  }

  @DisplayName("parse examples wrapped in outer list like in real response")
  @Test
  void parseExamplesRealShape() {

    var result =
        parser.parse(
            """
            [
              [["привет","hello",null,null,10]],
              null,
              "en",
              null,
              null,
              null,
              1,
              [],
              [["en"],null,[1],["en"]],
              null,
              null,
              null,
              null,
              [[["<b>hello</b> there, Katie!",null,null,null,null,"m_en_gbus0460730.012"]]]
            ]
            """);

    assertThat(result.getExamples()).containsExactly("hello there, Katie!");
  }

  @DisplayName("parse invalid json expects GoogleTranslatorException")
  @Test
  void parseInvalidJson() {
    assertThatThrownBy(() -> parser.parse("not a json"))
        .isInstanceOf(GoogleTranslatorException.class);
  }

  private String fullResponse() {

    return """
    [
      [
        ["привет","hello",null,null,10],
        ["hello",null,null,"həˈləʊ"]
      ],
      null,
      "en",
      null,
      null,
      [
        [
          "привет",
          null,
          [
            ["мелочь",0,true,false],
            ["не%20мелочь",0,true,false],
            ["пустяк",0,true,false]
          ]
        ]
      ],
      0.9353351,
      "hallo",
      [["en"],null,[0.9353351],["en"]],
      null,
      null,
      [
        [
          "noun",
          [
            [["приветствие","салют"],"noun.1"]
          ],
          "hello"
        ]
      ],
      [
        [
          "exclamation",
          [
            ["an expression of greeting","123.0","every morning they exchanged polite hellos"],
            ["used to begin a phone conversation","123.1",null]
          ],
          "hello"
        ],
        [
          "noun",
          [
            ["a greeting or reception","124.0","the proposal got a warm welcome"]
          ],
          "hello"
        ]
      ],
      [
        ["La <b>fourchette</b> des prix.",null],
        ["Another <b>example</b> sentence."]
      ],
      [
        ["hello",["hi","hey","howdy"],null,0.5],
        ["hullo",["hollo"],null,0.3]
      ]
    ]
    """;
  }
}
