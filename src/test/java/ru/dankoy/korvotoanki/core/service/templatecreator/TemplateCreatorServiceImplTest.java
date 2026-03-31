package ru.dankoy.korvotoanki.core.service.templatecreator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;
import ru.dankoy.korvotoanki.core.domain.anki.Definition;
import ru.dankoy.korvotoanki.core.domain.anki.Meaning;
import ru.dankoy.korvotoanki.core.service.templatebuilder.TemplateBuilder;

@DisplayName("Testing of TemplateCreatorServiceImpl service")
@ExtendWith(MockitoExtension.class)
public class TemplateCreatorServiceImplTest {

  @Mock private TemplateBuilder templateBuilder;

  @InjectMocks private TemplateCreatorServiceImpl service;

  @Test
  public void createTest() throws Exception {

    // given
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
    list.add(ankiData);

    var path = Paths.get(getClass().getResource("/templates/correct/correct-meaning.ftl").toURI());
    var correct = String.join(System.lineSeparator(), Files.readAllLines(path));

    when(templateBuilder.writeTemplate(anyMap(), anyString())).thenReturn(correct);

    // when
    String result = service.create(list);

    // then
    assertThat(result).isEqualTo(correct);
  }
}
