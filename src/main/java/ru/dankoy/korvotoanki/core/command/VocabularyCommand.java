package ru.dankoy.korvotoanki.core.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.service.objectmapper.ObjectMapperService;
import ru.dankoy.korvotoanki.core.service.title.TitleService;
import ru.dankoy.korvotoanki.core.service.vocabulary.VocabularyService;

@Component
@RequiredArgsConstructor
public class VocabularyCommand {

  private final VocabularyService vocabularyService;
  private final TitleService titleService;
  private final ObjectMapperService objectMapperService;

  @Command(
      group = "Vocabulary Commands",
      name = "vocabulary-count",
      alias = "vc",
      description = "Count all vocabulary")
  public String count() {
    Long count = vocabularyService.count();
    return objectMapperService.convertToString(count);
  }

  @Command(
      group = "Vocabulary Commands",
      name = "vocabulary-get-by-title",
      alias = "vgbt",
      description = "Get vocabulary by title")
  public String getByTitle(
      @Option(longName = "name", required = true, description = "title name") String titleName) {

    var title = titleService.getByName(titleName);
    List<Vocabulary> vocabulary = vocabularyService.getByTitle(title);
    return objectMapperService.convertToString(vocabulary);
  }

  @Command(
      group = "Vocabulary Commands",
      name = "vocabulary-get-all",
      alias = "vga",
      description = "Get all vocabulary")
  public String getAll() {
    List<Vocabulary> vocabulary = vocabularyService.getAll();
    return objectMapperService.convertToString(vocabulary);
  }
}
