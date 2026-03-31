package ru.dankoy.korvotoanki.core.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;
import ru.dankoy.korvotoanki.core.domain.anki.Definition;
import ru.dankoy.korvotoanki.core.domain.anki.Meaning;

@DisplayName("AnkiDataDTO tests ")
public class AnkiDataDTOTest {

  @DisplayName("toDTOTest with simple words")
  @Test
  public void toDTOTest() {

    var book = "book";
    var word = "word";
    var type = "type";
    var example = "my_example";
    var meaningC = "info";
    var transcription = "transcription";
    var translation = "translation1";

    var correctAnkiDataDTO =
        AnkiDataDTO.builder()
            .word(word)
            .book(book)
            // .meanings(meaningC)
            .myExample(example)
            .tags(List.of(type, book))
            .transcription(transcription)
            .translations(List.of(translation))
            .build();

    var meaning =
        new Meaning(
            type,
            Collections.singletonList(new Definition(type, example)),
            Collections.singletonList("synonym1"),
            Collections.singletonList("antonym1"));

    var ankiData =
        AnkiData.builder()
            .word(word)
            .book(book)
            .transcription(transcription)
            .myExample(example)
            .translations(Collections.singletonList(translation))
            .meanings(Collections.singletonList(meaning))
            .build();

    var result = AnkiDataDTO.toDTO(ankiData);

    assertThat(result).isEqualTo(correctAnkiDataDTO);
  }

  @DisplayName("toDTOTest with tags with white spaces")
  @Test
  public void toDTOTest_tagsWithWhitespaces() {

    var book = "book book1";
    var bookCorrect = "book_book1";
    var word = "word";
    var typeCorrect = "type_type1";
    var type = "type type1";
    var example = "my_example";
    var meaningC = "info";
    var transcription = "transcription";
    var translation = "translation1";

    var correctAnkiDataDTO =
        AnkiDataDTO.builder()
            .word(word)
            .book(book)
            // .meanings(meaningC)
            .myExample(example)
            .tags(List.of(typeCorrect, bookCorrect))
            .transcription(transcription)
            .translations(List.of(translation))
            .build();

    var meaning =
        new Meaning(
            type,
            Collections.singletonList(new Definition(type, example)),
            Collections.singletonList("synonym1"),
            Collections.singletonList("antonym1"));

    var ankiData =
        AnkiData.builder()
            .word(word)
            .book(book)
            .transcription(transcription)
            .myExample(example)
            .translations(Collections.singletonList(translation))
            .meanings(Collections.singletonList(meaning))
            .build();

    var result = AnkiDataDTO.toDTO(ankiData);

    assertThat(result).isEqualTo(correctAnkiDataDTO);
  }
}
