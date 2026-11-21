package ru.dankoy.korvotoanki.core.service.converter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.dankoy.korvotoanki.config.Languages;
import ru.dankoy.korvotoanki.config.appprops.ExternalApiProperties;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;
import ru.dankoy.korvotoanki.core.domain.dictionaryapi.Word;
import ru.dankoy.korvotoanki.core.domain.freedic.FreeDictionaryWord;
import ru.dankoy.korvotoanki.core.exceptions.DictionaryApiException;
import ru.dankoy.korvotoanki.core.exceptions.FreeDictionaryApiException;
import ru.dankoy.korvotoanki.core.fabric.anki.AnkiDataFabric;
import ru.dankoy.korvotoanki.core.service.dictionaryapi.DictionaryService;
import ru.dankoy.korvotoanki.core.service.freedic.FreeDictionaryService;
import ru.dankoy.korvotoanki.core.service.googletrans.GoogleTranslator;

@ConditionalOnProperty(prefix = "korvo-to-anki", value = "async", havingValue = "false")
@Slf4j
@Service
@RequiredArgsConstructor
public class AnkiConverterServiceImpl implements AnkiConverterService {

  private final DictionaryService dictionaryService;
  private final GoogleTranslator googleTranslator;
  private final AnkiDataFabric ankiDataFabric;
  private final ExternalApiProperties externalApiProperties;

  // not injected directly - the only implementation is based on WebClient, so the bean doesn't
  // exist in the deprecated ok-http mode
  private final ObjectProvider<FreeDictionaryService> freeDictionaryServiceProvider;

  public AnkiData convert(
      Vocabulary vocabulary, String sourceLanguage, String targetLanguage, List<String> options) {

    List<Word> daResult = Collections.singletonList(Word.emptyWord());
    var fdResult = FreeDictionaryWord.emptyWord();

    var isDictionaryApiEnabled = externalApiProperties.isDictionaryApiEnabled();
    var isFreeDictionaryApiEnabled = externalApiProperties.isFreeDictionaryApiEnabled();
    var isEnglish = sourceLanguage.equals(Languages.EN.name().toLowerCase());
    var isAuto = sourceLanguage.equals(Languages.AUTO.name().toLowerCase());

    // ignore auto source language because won't know the defined language. Look up for Tika
    // Language Detection
    if (isDictionaryApiEnabled && isEnglish) {

      try {
        daResult = dictionaryService.define(vocabulary.word());
      } catch (DictionaryApiException e) {
        log.warn(
            String.format(
                "Couldn't get definition from dictionaryapi.dev for '%s' - %s",
                vocabulary.word(), e.getMessage()));
      }
    }

    var freeDictionaryService = freeDictionaryServiceProvider.getIfAvailable();

    // freedictionaryapi works with any language, not only english, so it is skipped only when the
    // source language is unknown (auto)
    if (isFreeDictionaryApiEnabled && !isAuto && Objects.nonNull(freeDictionaryService)) {

      try {
        fdResult = freeDictionaryService.define(vocabulary.word(), sourceLanguage);
      } catch (FreeDictionaryApiException | DictionaryApiException e) {
        log.warn(
            String.format(
                "Couldn't get definition from freedictionaryapi.com for '%s' - %s",
                vocabulary.word(), e.getMessage()));
      }
    }

    var gtResult =
        googleTranslator.translate(vocabulary.word(), targetLanguage, sourceLanguage, options);

    if (Objects.nonNull(gtResult.getPossibleMistake())) {
      log.info(
          String.format(
              "Google translate found possible mistake for '%s'. Maybe you meant '%s'",
              vocabulary.word(), gtResult.getPossibleMistake()));
    }

    return ankiDataFabric.createAnkiData(vocabulary, gtResult, daResult, fdResult);
  }
}
