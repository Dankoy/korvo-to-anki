package ru.dankoy.korvotoanki.core.command;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;
import ru.dankoy.korvotoanki.core.service.dictionaryapi.DictionaryService;
import ru.dankoy.korvotoanki.core.service.objectmapper.ObjectMapperService;

@Component
@RequiredArgsConstructor
public class DictionaryApiCommand {

  private final DictionaryService dictionaryService;
  private final ObjectMapperService objectMapperService;

  // da --word hello
  @Command(
      group = "Dictionary Api Commands",
      name = "dictionary-api",
      alias = "da",
      description = "Define word using dictionaryapi.dev",
      help =
          """
          SYNOPSIS
              dictionary-api --word String

          OPTIONS
              --word String
             text to define
             [Required]

             --help or -h
             help for dictionary-api
             [Optional]
          """)
  public String define(
      @Option(longName = "word", required = true, description = "text to define") String word) {

    var translatedString = dictionaryService.define(word);
    return objectMapperService.convertToString(translatedString);
  }
}
