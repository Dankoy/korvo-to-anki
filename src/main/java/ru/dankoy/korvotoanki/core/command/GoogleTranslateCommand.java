package ru.dankoy.korvotoanki.core.command;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;
import ru.dankoy.korvotoanki.core.service.googletrans.GoogleTranslator;
import ru.dankoy.korvotoanki.core.service.objectmapper.ObjectMapperService;

@Component
@RequiredArgsConstructor
public class GoogleTranslateCommand {

  private final GoogleTranslator googleTranslator;
  private final ObjectMapperService objectMapperService;

  // gt --text hello --options t,at,md,bd
  @Command(
      group = "Google Translate Commands",
      name = "google-translate",
      alias = "gt",
      description = "Translate text using google translate")
  public String translate(
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

    List<String> optionsList = Arrays.asList(options);

    var translatedString =
        googleTranslator.translate(text, targetLanguage, sourceLanguage, optionsList);
    return objectMapperService.convertToString(translatedString);
  }
}
