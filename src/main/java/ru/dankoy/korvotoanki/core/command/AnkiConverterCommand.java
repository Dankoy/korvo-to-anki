package ru.dankoy.korvotoanki.core.command;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.service.converter.AnkiConverterService;
import ru.dankoy.korvotoanki.core.service.objectmapper.ObjectMapperService;

@Component
@RequiredArgsConstructor
public class AnkiConverterCommand {

  private final AnkiConverterService ankiConverterService;
  private final ObjectMapperService objectMapperService;

  // ac --word hello
  @Command(
      group = "Anki converter Commands",
      name = "anki-converter",
      alias = "ac",
      description = "Translate and define text using google translator and dictionaryapi.dev")
  public String translateAndConvert(
      @Option(longName = "text", required = true, description = "text to translate") String text,
      @Option(
              longName = "sourceLanguage",
              required = false,
              defaultValue = "en",
              description = "source language")
          String sourceLanguage,
      @Option(
              longName = "targetLanguage",
              required = false,
              defaultValue = "ru",
              description = "target language")
          String targetLanguage,
      @Option(
              longName = "options",
              required = false,
              defaultValue = "t,at,md,rm",
              description = "options")
          String[] options) {

    var vocabulary = new Vocabulary(text, new Title(0, null, 0L), 0L, 0L, 0L, 0L, null, null, 0L);

    List<String> optionsList = Arrays.asList(options);
    var ankiData =
        ankiConverterService.convert(vocabulary, sourceLanguage, targetLanguage, optionsList);

    return objectMapperService.convertToString(ankiData);
  }
}
