package ru.dankoy.korvotoanki.core.service.converter;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import ru.dankoy.korvotoanki.config.Languages;
import ru.dankoy.korvotoanki.config.appprops.ExternalApiProperties;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;
import ru.dankoy.korvotoanki.core.domain.dictionaryapi.Word;
import ru.dankoy.korvotoanki.core.exceptions.DictionaryApiException;
import ru.dankoy.korvotoanki.core.exceptions.GoogleTranslatorException;
import ru.dankoy.korvotoanki.core.fabric.anki.AnkiDataFabric;
import ru.dankoy.korvotoanki.core.service.dictionaryapi.DictionaryService;
import ru.dankoy.korvotoanki.core.service.googletrans.GoogleTranslator;

@ConditionalOnExpression(
    """
        (${korvo-to-anki.async} and '${korvo-to-anki.async-type}'.equals('completable_future'))
        or
        (${korvo-to-anki.async} and '${korvo-to-anki.async-type}'.equals('vtcf'))
    """)
@Slf4j
@Service
@RequiredArgsConstructor
public class AnkiConverterServiceCompletableFuture implements AnkiConverterService {

  private final DictionaryService dictionaryService;
  private final GoogleTranslator googleTranslator;
  private final AnkiDataFabric ankiDataFabric;
  private final ExternalApiProperties externalApiProperties;
  private final ExecutorService ankiConverterTaskExecutor;

  public AnkiData convert(
      Vocabulary vocabulary, String sourceLanguage, String targetLanguage, List<String> options) {

    var isDictionaryApiEnabled = externalApiProperties.isDictionaryApiEnabled();

    log.debug(String.format("Working with word: '%s'", vocabulary.word()));

    // ignore auto source language because won't know the defined language. Look up
    // for Tika Language Detection
    var cf1 =
        CompletableFuture.supplyAsync(
                () -> {
                  if (isDictionaryApiEnabled
                      && sourceLanguage.equals(Languages.EN.name().toLowerCase()))
                    return dictionaryService.define(vocabulary.word());
                  else return Collections.singletonList(Word.emptyWord());
                },
                ankiConverterTaskExecutor)
            .handle(
                (result, ex) -> {
                  if (ex != null && ex.getCause() instanceof DictionaryApiException) {
                    log.warn(
                        String.format(
                            "Couldn't get definition from dictionaryapi.dev for '%s' - %s",
                            vocabulary.word(), ex.getMessage()));
                    return Collections.singletonList(Word.emptyWord());
                  }
                  return result;
                });

    var cf2 =
        CompletableFuture.supplyAsync(
                () ->
                    googleTranslator.translate(
                        vocabulary.word(), targetLanguage, sourceLanguage, options),
                ankiConverterTaskExecutor)
            .handle(
                (result, ex) -> {
                  // it wraps exceptions in CompletionException
                  if (ex != null && ex.getCause() instanceof GoogleTranslatorException gte) {
                    log.error(
                        String.format(
                            "Couldn't translate '%s' from %s to %s - %s",
                            vocabulary.word(), sourceLanguage, targetLanguage, ex.getMessage()));
                    throw gte;
                  }
                  return result;
                });

    return CompletableFuture.allOf(cf1, cf2)
        .thenApply(
            ignored -> {
              return ankiDataFabric.createAnkiData(vocabulary, cf2.join(), cf1.join());
            })
        .join();
  }
}
