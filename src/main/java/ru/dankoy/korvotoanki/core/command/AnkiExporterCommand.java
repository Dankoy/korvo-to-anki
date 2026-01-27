package ru.dankoy.korvotoanki.core.command;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;
import ru.dankoy.korvotoanki.core.service.exporter.ExporterService;

@Component
@Slf4j
@RequiredArgsConstructor
public class AnkiExporterCommand {

  private final ExporterService exporterService;

  // ae --word hello
  @Command(
      group = "Anki exporter Commands",
      name = "anki-exporter",
      alias = "ae",
      description =
          "Export to anki. Translate and define text using google translator and dictionaryapi.dev")
  public String export(
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

    long startTime = System.currentTimeMillis();

    exporterService.export(sourceLanguage, targetLanguage, optionsList);

    long finishTime = System.currentTimeMillis();
    log.info("Export took: " + (finishTime - startTime) / 1000 + " s");

    return "done";
  }
}
