package ru.dankoy.korvotoanki.core.service.converter;


import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

  public AnkiData convert(Vocabulary vocabulary, String sourceLanguage, String targetLanguage,
      List<String> options) {

    List<Word> daResult = Collections.singletonList(Word.emptyWord());
    try {
      daResult = dictionaryService.define(vocabulary.word());
    } catch (DictionaryApiException e) {
      log.warn(String.format("Couldn't get definition from dictionaryapi.dev - %s", e));
    }

    var gtResult = googleTranslator.translate(vocabulary.word(), targetLanguage, sourceLanguage,
        options);

    return ankiDataFabric.createAnkiData(vocabulary, gtResult, daResult);

  }

}
