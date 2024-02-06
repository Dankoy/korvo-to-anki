package ru.dankoy.korvotoanki.core.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.service.objectmapper.ObjectMapperService;
import ru.dankoy.korvotoanki.core.service.title.TitleService;
import ru.dankoy.korvotoanki.core.service.vocabulary.VocabularyService;

@RequiredArgsConstructor
@ShellComponent
public class VocabularyCommand {

  private final VocabularyService vocabularyService;
  private final TitleService titleService;
  private final ObjectMapperService objectMapperService;

  @ShellMethod(
      key = {"vocabulary-count", "vc"},
      value = "Count all vocabulary")
  public String count() {
    Long count = vocabularyService.count();
    return objectMapperService.convertToString(count);
  }

  @ShellMethod(
      key = {"vocabulary-get-by-title", "vgbt"},
      value = "Get vocabulary by title")
  public String getByTitle(@ShellOption String titleName) {

    var title = titleService.getByName(titleName);
    List<Vocabulary> vocabulary = vocabularyService.getByTitle(title);
    return objectMapperService.convertToString(vocabulary);
  }

  @ShellMethod(
      key = {"vocabulary-get-all", "vga"},
      value = "Get all vocabulary")
  public String getAll() {
    List<Vocabulary> vocabulary = vocabularyService.getAll();
    return objectMapperService.convertToString(vocabulary);
  }
}
