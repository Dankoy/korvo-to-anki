package ru.dankoy.korvotoanki.core.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;

/** Used in freemarker template builder */
@Getter
@Builder
public class AnkiDataDTO {

  private final String word;
  private final String book;
  private final String myExample;
  private final List<String> translations; // from google translate
  private final String
      transcription; // from dictionaryapi, if not found then from Google Translate or null
  private final List<String> tags;

  @Setter private String meanings; // html string

  public static AnkiDataDTO toDTO(AnkiData ankiData) {

    var example = ankiData.getMyExample();
    if (Objects.nonNull(example)) {
      example = example.replace("\t", "");
      example = example.replace("\n", "");
    }

    List<String> tags =
        ankiData.getMeanings().stream()
            .map(m -> m.type())
            .map(t -> t.toLowerCase())
            .distinct()
            .collect(Collectors.toCollection(ArrayList::new));
    tags.add(ankiData.getBook().toLowerCase().replace(" ", "_"));

    return AnkiDataDTO.builder()
        .word(ankiData.getWord())
        .book(ankiData.getBook())
        .myExample(example)
        .transcription(ankiData.getTranscription())
        .translations(new ArrayList<>(ankiData.getTranslations()))
        .tags(tags)
        .build();
  }
}
