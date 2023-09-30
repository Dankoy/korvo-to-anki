package ru.dankoy.korvotoanki.core.command;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import ru.dankoy.korvotoanki.core.service.googletrans.GoogleTranslator;
import ru.dankoy.korvotoanki.core.service.objectmapper.ObjectMapperService;

@Command(group = "Google Translate Commands")
@RequiredArgsConstructor
public class GoogleTranslateCommand {

  private final GoogleTranslator googleTranslator;
  private final ObjectMapperService objectMapperService;


  // gt --text hello --options t,at,md,bd
  @Command(command = "google-translate",
      alias = "gt",
      description = "Translate text using google translate")
  public String translate(
      @Option(required = true, description = "text to translate") String text,
      @Option(required = false, defaultValue = "auto", description = "source language") String sourceLanguage,
      @Option(required = false, defaultValue = "ru", description = "target language") String targetLanguage,
      @Option(required = false, defaultValue = "t,at,md,rm", description = "options") String[] options
  ) {

    List<String> optionsList = Arrays.asList(options);

    var translatedString = googleTranslator.translate(text, targetLanguage, sourceLanguage,
        optionsList);
    return objectMapperService.convertToString(translatedString);
  }


}
