package ru.dankoy.korvotoanki.core.fabric.anki;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;
import ru.dankoy.korvotoanki.core.domain.anki.Definition;
import ru.dankoy.korvotoanki.core.domain.anki.Meaning;
import ru.dankoy.korvotoanki.core.domain.dictionaryapi.Phonetics;
import ru.dankoy.korvotoanki.core.domain.dictionaryapi.Word;
import ru.dankoy.korvotoanki.core.domain.googletranslation.GoogleTranslation;

@Component
public class AnkiDataFabricImpl implements AnkiDataFabric {

  @Override
  public AnkiData createAnkiData(
      Vocabulary vocabulary, GoogleTranslation googleTranslation, List<Word> words) {

    return AnkiData.builder()
        .word(vocabulary.word())
        .book(vocabulary.title().name())
        .translations(googleTranslation.getTranslations()) // could be empty list
        .transcription(chooseTranscription(googleTranslation, words))
        .myExample(obtainVocabularyExample(vocabulary))
        .meanings(chooseDefinitions(googleTranslation, words))
        .build();
  }

  private String obtainVocabularyExample(Vocabulary vocabulary) {

    if (Objects.nonNull(vocabulary.prevContext())) {
      return vocabulary.prevContext() + vocabulary.word() + vocabulary.nextContext();
    } else {
      return null;
    }
  }

  private String chooseTranscription(GoogleTranslation googleTranslation, List<Word> words) {

    var dictionaryApiTranscription =
        words.stream().map(Word::getPhonetic).filter(Objects::nonNull).findFirst();

    if (dictionaryApiTranscription.isEmpty()) {

      // get first non-empty or null transcription
      var dictionaryApiTranscriptionOptional =
          words.stream()
              .flatMap(w -> Stream.of(w.getPhonetics()))
              .flatMap(List::stream)
              .map(Phonetics::getText)
              .filter(Objects::nonNull)
              .findFirst();

      // either dictionaryapi transcription or Google Translate transcription
      return dictionaryApiTranscriptionOptional.orElse(googleTranslation.getTranscription());

    } else {
      return dictionaryApiTranscription.get();
    }
  }

  private List<Meaning> chooseDefinitions(GoogleTranslation googleTranslation, List<Word> words) {

    List<Meaning> result = new ArrayList<>();

    // convert to anki definition
    for (Word daWord : words) {

      for (ru.dankoy.korvotoanki.core.domain.dictionaryapi.Meaning daMeaning :
          daWord.getMeanings()) {

        var partOfSpeech = daMeaning.getPartOfSpeech();
        List<String> daSynonyms = daMeaning.getSynonyms();
        List<String> daAntonyms = daMeaning.getAntonyms();

        var ankiDefs =
            daMeaning.getDefinitions().stream()
                .map(d -> new Definition(d.getInfo(), d.getExample()))
                .toList();

        result.add(new Meaning(partOfSpeech, ankiDefs, daSynonyms, daAntonyms));
      }
    }

    // if data from dictionaryapi is empty then take from Google translation
    if (result.isEmpty()) {

      googleTranslation
          .getDefinitions()
          .forEach(
              gtd ->
                  result.add(
                      new Meaning(
                          gtd.type(),
                          Collections.singletonList(new Definition(gtd.info(), null)),
                          new ArrayList<>(),
                          new ArrayList<>())));
    }

    return result;
  }
}
