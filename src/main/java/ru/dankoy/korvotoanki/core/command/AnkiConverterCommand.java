package ru.dankoy.korvotoanki.core.command;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.service.converter.AnkiConverterService;
import ru.dankoy.korvotoanki.core.service.objectmapper.ObjectMapperService;

@Command(group = "Anki converter Commands")
@RequiredArgsConstructor
public class AnkiConverterCommand {

  private final AnkiConverterService ankiConverterService;
  private final ObjectMapperService objectMapperService;


  // ac --word hello
  @Command(command = "anki-converter",
      alias = "ac",
      description = "Translate and define text using google translator and dictionaryapi.dev")
  public String translateAndConvert(
      @Option(required = true, description = "text to translate") String text,
      @Option(required = false, defaultValue = "auto", description = "source language") String sourceLanguage,
      @Option(required = false, defaultValue = "ru", description = "target language") String targetLanguage,
      @Option(required = false, defaultValue = "t,at,md,rm", description = "options") String[] options
  ) {

    var vocabulary = new Vocabulary(text,
        null,
        0L,
        0L,
        0L,
        0L,
        null,
        null,
        0L);

    List<String> optionsList = Arrays.asList(options);
    var ankiData = ankiConverterService.convert(vocabulary, sourceLanguage, targetLanguage, optionsList);

    return objectMapperService.convertToString(ankiData);
  }


}
