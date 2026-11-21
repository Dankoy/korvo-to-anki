package ru.dankoy.korvotoanki.core.service.converter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.dankoy.korvotoanki.config.Languages;
import ru.dankoy.korvotoanki.config.appprops.ExternalApiProperties;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;
import ru.dankoy.korvotoanki.core.domain.dictionaryapi.Word;
import ru.dankoy.korvotoanki.core.domain.freedic.FreeDictionaryWord;
import ru.dankoy.korvotoanki.core.exceptions.DictionaryApiException;
import ru.dankoy.korvotoanki.core.exceptions.FreeDictionaryApiException;
import ru.dankoy.korvotoanki.core.exceptions.GoogleTranslatorException;
import ru.dankoy.korvotoanki.core.fabric.anki.AnkiDataFabric;
import ru.dankoy.korvotoanki.core.service.dictionaryapi.DictionaryService;
import ru.dankoy.korvotoanki.core.service.freedic.FreeDictionaryService;
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

  // not injected directly - the only implementation is based on WebClient, so the bean doesn't
  // exist in the deprecated ok-http mode
  private final ObjectProvider<FreeDictionaryService> freeDictionaryServiceProvider;

  public AnkiData convert(
      Vocabulary vocabulary, String sourceLanguage, String targetLanguage, List<String> options) {

    var isDictionaryApiEnabled = externalApiProperties.isDictionaryApiEnabled();
    var isFreeDictionaryApiEnabled = externalApiProperties.isFreeDictionaryApiEnabled();
    var isEnglish = sourceLanguage.equals(Languages.EN.name().toLowerCase());
    var freeDictionaryService = freeDictionaryServiceProvider.getIfAvailable();

    log.debug(String.format("Working with word: '%s'", vocabulary.word()));

    // ignore auto source language because won't know the defined language. Look up
    // for Tika Language Detection
    var cf1 =
        CompletableFuture.supplyAsync(
                () -> {
                  if (isDictionaryApiEnabled && isEnglish)
                    return dictionaryService.define(vocabulary.word());
                  else return Collections.singletonList(Word.emptyWord());
                },
                ankiConverterTaskExecutor)
            .handle(
                (result, ex) -> {
                  log.atDebug()
                      .setMessage("Handle exception for word {}: {}")
                      .addArgument(vocabulary.word())
                      .addArgument(ex)
                      .log();
                  if (ex != null
                      && (ex.getCause() instanceof DictionaryApiException
                          || ex.getCause() instanceof WebClientResponseException
                          || ex.getCause() instanceof WebClientRequestException)) {
                    log.warn(
                        String.format(
                            "Couldn't get definition from dictionaryapi.dev for '%s' with cause"
                                + " '%s'. Fallback to empty word.",
                            vocabulary.word(), ex.getCause().getMessage()));
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

    var cf3 =
        CompletableFuture.supplyAsync(
                () -> {
                  if (isFreeDictionaryApiEnabled
                      && isEnglish
                      && Objects.nonNull(freeDictionaryService))
                    return freeDictionaryService.define(vocabulary.word());
                  else return FreeDictionaryWord.emptyWord();
                },
                ankiConverterTaskExecutor)
            .handle(
                (result, ex) -> {
                  log.atDebug()
                      .setMessage("Handle exception for word {}: {}")
                      .addArgument(vocabulary.word())
                      .addArgument(ex)
                      .log();
                  if (ex != null
                      && (ex.getCause() instanceof FreeDictionaryApiException
                          || ex.getCause() instanceof DictionaryApiException
                          || ex.getCause() instanceof WebClientResponseException
                          || ex.getCause() instanceof WebClientRequestException)) {
                    log.warn(
                        String.format(
                            "Couldn't get definition from freedictionaryapi.com for '%s' with cause"
                                + " '%s'. Fallback to empty word.",
                            vocabulary.word(), ex.getCause().getMessage()));
                    return FreeDictionaryWord.emptyWord();
                  }
                  return result;
                });

    return CompletableFuture.allOf(cf1, cf2, cf3)
        .thenApply(
            ignored -> {
              var gtResult = cf2.join();
              if (Objects.nonNull(gtResult.getPossibleMistake())) {
                log.info(
                    String.format(
                        "Google translate found possible mistake for '%s'. Maybe you meant '%s'",
                        vocabulary.word(), gtResult.getPossibleMistake()));
              }
              return ankiDataFabric.createAnkiData(vocabulary, gtResult, cf1.join(), cf3.join());
            })
        .join();
  }
}
