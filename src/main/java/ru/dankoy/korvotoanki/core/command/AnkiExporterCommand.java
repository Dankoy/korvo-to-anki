package ru.dankoy.korvotoanki.core.command;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import ru.dankoy.korvotoanki.core.service.exporter.ExporterService;

@Slf4j
@Command(group = "Anki exporter Commands")
@RequiredArgsConstructor
public class AnkiExporterCommand {

  private final ExporterService exporterService;


  // ae --word hello
  @Command(command = "anki-exporter",
      alias = "ae",
      description = "Export to anki. Translate and define text using google translator and dictionaryapi.dev")
  public String export(
      @Option(required = false, defaultValue = "en", description = "source language") String sourceLanguage,
      @Option(required = false, defaultValue = "ru", description = "target language") String targetLanguage,
      @Option(required = false, defaultValue = "t,at,md,rm", description = "options") String[] options
  ) {

    List<String> optionsList = Arrays.asList(options);

    long startTime = System.currentTimeMillis();

    exporterService.export(sourceLanguage, targetLanguage, optionsList);

    long finishTime = System.currentTimeMillis();
    log.info("Export took: " + (finishTime - startTime) / 1000 + " s");

    return "done";
  }


}
