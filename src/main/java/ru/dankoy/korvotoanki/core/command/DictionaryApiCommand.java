package ru.dankoy.korvotoanki.core.command;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import ru.dankoy.korvotoanki.core.service.dictionaryapi.DictionaryService;
import ru.dankoy.korvotoanki.core.service.objectmapper.ObjectMapperService;

@Command(group = "Dictionary Api Commands")
@RequiredArgsConstructor
public class DictionaryApiCommand {

  private final DictionaryService dictionaryService;
  private final ObjectMapperService objectMapperService;


  // da --word hello
  @Command(command = "dictionary-api",
      alias = "da",
      description = "Define word using dictionaryapi.dev")
  public String define(
      @Option(required = true, description = "text to translate") String word
  ) {

    var translatedString = dictionaryService.define(word);
    return objectMapperService.convertToString(translatedString);
  }


}
