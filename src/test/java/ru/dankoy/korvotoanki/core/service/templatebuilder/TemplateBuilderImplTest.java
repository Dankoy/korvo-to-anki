package ru.dankoy.korvotoanki.core.service.templatebuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.dankoy.korvotoanki.config.TemplateBuilderConfig;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;
import ru.dankoy.korvotoanki.core.domain.anki.Definition;
import ru.dankoy.korvotoanki.core.domain.anki.Meaning;
import ru.dankoy.korvotoanki.core.dto.AnkiDataDTO;
import ru.dankoy.korvotoanki.core.exceptions.KorvoRootException;

@SpringBootTest(classes = {TemplateBuilder.class, TemplateBuilderConfig.class})
// @Import(value = TemplateBuilderConfig.class)
@DisplayName("Test TemplateBuilderImpl ")
class TemplateBuilderImplTest {

  @Autowired private TemplateBuilder templateBuilder;

  @DisplayName("writeTemplate meaning export throws exception")
  @Test
  void writeTemplateMeaningThrowsException() {

    Map<String, Object> templateData = new HashMap<>();

    assertThatThrownBy(() -> templateBuilder.writeTemplate(templateData, "meaning.ftl"))
        .isInstanceOf(KorvoRootException.class);
  }

  @DisplayName("writeTemplate anki export")
  @Test
  void writeTemplateMeaning() throws IOException, URISyntaxException {

    List<AnkiData> list = new ArrayList<>();

    var meaning =
        new Meaning(
            "type",
            Collections.singletonList(new Definition("info", "example")),
            Collections.singletonList("synonym1"),
            Collections.singletonList("antonym1"));

    var ankiData =
        AnkiData.builder()
            .word("word")
            .book("book")
            .transcription("transcription")
            .myExample("my_example")
            .translations(Collections.singletonList("translation1"))
            .meanings(Collections.singletonList(meaning))
            .build();

    var path = Paths.get(getClass().getResource("/templates/correct/correct-meaning.ftl").toURI());
    var correct = String.join(System.lineSeparator(), Files.readAllLines(path));

    Map<String, Object> templateData = new HashMap<>();
    templateData.put("ankiData", ankiData);
    var t = templateBuilder.writeTemplate(templateData, "meaning.ftl");

    assertThat(t).isEqualTo(correct);
  }

  @DisplayName("writeTemplate meaning export")
  @Test
  void writeTemplateAnkiExport() throws IOException, URISyntaxException {

    var ankiData =
        AnkiDataDTO.builder()
            .word("word")
            .book("book")
            .transcription("transcription")
            .myExample("my_example")
            .translations(Collections.singletonList("translation1"))
            .meanings("meanings")
            .build();

    var path =
        Paths.get(
            getClass().getResource("/templates/correct/correct-korvo-to-anki-correct.ftl").toURI());
    var correct = String.join(System.lineSeparator(), Files.readAllLines(path));

    Map<String, Object> templateData = new HashMap<>();
    templateData.put("ankiDataList", Collections.singletonList(ankiData));
    var t = templateBuilder.writeTemplate(templateData, "korvo-to-anki.ftl");

    assertThat(t).isEqualTo(correct);
  }
}
