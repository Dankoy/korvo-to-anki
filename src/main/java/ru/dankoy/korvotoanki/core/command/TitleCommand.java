package ru.dankoy.korvotoanki.core.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.service.objectmapper.ObjectMapperService;
import ru.dankoy.korvotoanki.core.service.title.TitleService;

@RequiredArgsConstructor
@ShellComponent
public class TitleCommand {

  private final TitleService titleService;
  private final ObjectMapperService objectMapperService;

  @ShellMethod(
      key = {"title-count", "tc"},
      value = "Count all titles")
  public String count() {
    Long count = titleService.count();
    return objectMapperService.convertToString(count);
  }

  @ShellMethod(
      key = {"title-get-by-id", "tgbi"},
      value = "Get title by id")
  public String getById(@ShellOption long id) {
    Title title = titleService.getById(id);
    return objectMapperService.convertToString(title);
  }

  @ShellMethod(
      key = {"title-get-all", "tga"},
      value = "Get all titles")
  public String getAll() {
    List<Title> titles = titleService.getAll();
    return objectMapperService.convertToString(titles);
  }

  @ShellMethod(
      key = {"title-insert", "ti"},
      value = "Insert new title")
  public String insert(@ShellOption String titleName) {
    long id = titleService.insert(titleName);
    return objectMapperService.convertToString(id);
  }

  @ShellMethod(
      key = {"title-delete", "td"},
      value = "Delete title by id")
  public String deleteById(@ShellOption long id) {
    titleService.deleteById(id);
    return String.format("Deleted title with id - %d", id);
  }

  @ShellMethod(
      key = {"title-update", "tu"},
      value = "Update title")
  public String update(
      @ShellOption long id, @ShellOption String authorName, @ShellOption long filter) {
    Title title = new Title(id, authorName, filter);
    titleService.update(title);
    return String.format("Updated title - %s", objectMapperService.convertToString(title));
  }
}
