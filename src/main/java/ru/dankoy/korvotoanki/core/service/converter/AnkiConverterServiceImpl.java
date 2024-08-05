package ru.dankoy.korvotoanki.core.service.converter;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.dankoy.korvotoanki.config.Languages;
import ru.dankoy.korvotoanki.config.appprops.ExternalApiProperties;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;
import ru.dankoy.korvotoanki.core.domain.dictionaryapi.Word;
import ru.dankoy.korvotoanki.core.exceptions.DictionaryApiException;
import ru.dankoy.korvotoanki.core.fabric.anki.AnkiDataFabric;
import ru.dankoy.korvotoanki.core.service.dictionaryapi.DictionaryService;
import ru.dankoy.korvotoanki.core.service.googletrans.GoogleTranslator;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnkiConverterServiceImpl implements AnkiConverterService {

  private final DictionaryService dictionaryService;
  private final GoogleTranslator googleTranslator;
  private final AnkiDataFabric ankiDataFabric;
  private final ExternalApiProperties externalApiProperties;

  public AnkiData convert(
      Vocabulary vocabulary, String sourceLanguage, String targetLanguage, List<String> options) {

    List<Word> daResult = Collections.singletonList(Word.emptyWord());

    var isDictionaryApiEnabled = externalApiProperties.isDictionaryApiEnabled();

    // ignore auto source language because won't know the defined language. Look up for Tika
    // Language Detection
    if (isDictionaryApiEnabled && sourceLanguage.equals(Languages.EN.name().toLowerCase())) {

      try {
        daResult = dictionaryService.define(vocabulary.word());
      } catch (DictionaryApiException e) {
        log.warn(
            String.format(
                "Couldn't get definition from dictionaryapi.dev for '%s' - %s",
                vocabulary.word(), e.getMessage()));
      }
    }

    var gtResult =
        googleTranslator.translate(vocabulary.word(), targetLanguage, sourceLanguage, options);

    return ankiDataFabric.createAnkiData(vocabulary, gtResult, daResult);
  }
}
