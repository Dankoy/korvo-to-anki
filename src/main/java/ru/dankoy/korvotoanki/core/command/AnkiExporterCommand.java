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
      exitStatusExceptionMapper = "korvoExitStatusExceptionMapper",
      group = "Anki exporter Commands",
      name = "anki-exporter",
      alias = "ae",
      description =
          "Export to anki. Translate and define text using google translator and dictionaryapi.dev",
      help =
          """
          SYNOPSIS
              anki-exporter --sourceLanguage String --targetLanguage String --options String[] --books String

          OPTIONS
              --sourceLanguage String
             source language
             [Optional, default = en]

             --targetLanguage String
             target language
             [Optional, default = ru]

             --options String[]
             options
             [Optional, default = t,at,md,rm,ss,ex,qca]

             --books String
             semicolon-separated book names to export words from; book names may
             contain commas and spaces; empty means all books
             [Optional, default = all books]

             --help or -h
             help for anki-exporter
             [Optional]
          """)
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
              defaultValue = "t,at,md,rm,ss,ex,qca",
              description = "options")
          String[] options,
      @Option(longName = "books", required = false, description = "book names to export words from")
          String books) {

    List<String> optionsList = Arrays.asList(options);
    List<String> booksList =
        books == null
            ? List.of()
            : Arrays.stream(books.split(";")).map(String::trim).filter(b -> !b.isEmpty()).toList();

    long startTime = System.currentTimeMillis();

    exporterService.export(sourceLanguage, targetLanguage, optionsList, booksList);

    long finishTime = System.currentTimeMillis();
    log.info("Export took: " + (finishTime - startTime) / 1000 + " s");

    return "done";
  }
}
