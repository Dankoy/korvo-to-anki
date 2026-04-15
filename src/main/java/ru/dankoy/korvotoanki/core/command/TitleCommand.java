package ru.dankoy.korvotoanki.core.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.service.objectmapper.ObjectMapperService;
import ru.dankoy.korvotoanki.core.service.title.TitleService;

@Component
@RequiredArgsConstructor
public class TitleCommand {

  private final TitleService titleService;
  private final ObjectMapperService objectMapperService;

  @Command(
      group = "Titles Commands",
      name = "title-count",
      alias = "tc",
      description = "Count all titles",
      help =
          """
          SYNOPSIS
              title-count

          OPTIONS
             --help or -h
             help for title-get-by-id
             [Optional]
          """)
  public String count() {
    Long count = titleService.count();
    return objectMapperService.convertToString(count);
  }

  @Command(
      group = "Titles Commands",
      name = "title-get-by-id",
      alias = "tgbi",
      description = "Get title by id",
      help =
          """
          SYNOPSIS
              title-get-by-id --id Long

          OPTIONS
              --id Long
             title id
             [Required]

             --help or -h
             help for title-get-by-id
             [Optional]
          """)
  public String getById(
      @Option(longName = "id", required = true, description = "title id") long id) {
    Title title = titleService.getById(id);
    return objectMapperService.convertToString(title);
  }

  @Command(
      group = "Titles Commands",
      name = "title-get-all",
      alias = "tga",
      description = "Get all titles",
      help =
          """
          SYNOPSIS
              title-get-all

          OPTIONS
             --help or -h
             help for title-get-all
             [Optional]
          """)
  public String getAll() {
    List<Title> titles = titleService.getAll();
    return objectMapperService.convertToString(titles);
  }

  @Command(
      group = "Titles Commands",
      name = "title-insert",
      alias = "ti",
      description = "Insert new title",
      help =
          """
          SYNOPSIS
              title-insert --titleName String

          OPTIONS
              --titleName String
             title name
             [Required]

             --help or -h
             help for title-insert
             [Optional]
          """)
  public String insert(
      @Option(longName = "titleName", required = true, description = "title name")
          String titleName) {
    long id = titleService.insert(titleName);
    return objectMapperService.convertToString(id);
  }

  @Command(
      group = "Titles Commands",
      name = "title-delete",
      alias = "td",
      description = "Delete title by id",
      help =
          """
          SYNOPSIS
              title-delete --id Long

          OPTIONS
              --id Long
             title id
             [Required]

             --help or -h
             help for title-delete
             [Optional]
          """)
  public String deleteById(
      @Option(longName = "id", required = true, description = "title id") long id) {
    titleService.deleteById(id);
    return String.format("Deleted title with id - %d", id);
  }

  @Command(
      group = "Titles Commands",
      name = "title-update",
      alias = "tu",
      description = "Update title",
      help =
          """
          SYNOPSIS
              title-update --id Long

          OPTIONS
              --id Long
             title name
             [Required]

             --authorName String
             author name
             [Required]

             --filter Long
             filter (1 or 0)
             [Required]

             --help or -h
             help for title-update
             [Optional]
          """)
  public String update(
      @Option(longName = "id", required = true, description = "title id") long id,
      @Option(longName = "authorName", required = true, description = "author name")
          String authorName,
      @Option(longName = "filter", required = true, description = "filter") long filter) {
    Title title = new Title(id, authorName, filter);
    titleService.update(title);
    return String.format("Updated title - %s", objectMapperService.convertToString(title));
  }
}
