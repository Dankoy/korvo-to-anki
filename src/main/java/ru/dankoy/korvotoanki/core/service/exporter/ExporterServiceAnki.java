package ru.dankoy.korvotoanki.core.service.exporter;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;
import ru.dankoy.korvotoanki.core.exceptions.KorvoRootException;
import ru.dankoy.korvotoanki.core.service.converter.AnkiConverterService;
import ru.dankoy.korvotoanki.core.service.io.IOService;
import ru.dankoy.korvotoanki.core.service.templatecreator.TemplateCreatorService;
import ru.dankoy.korvotoanki.core.service.vocabulary.VocabularyService;


@Slf4j
@ConditionalOnProperty(prefix = "korvo-to-anki", value = "async", havingValue = "false")
@Service
@RequiredArgsConstructor
public class ExporterServiceAnki implements ExporterService {

  private static final int STEP_SIZE = 10;
  private int counter = 0;

  private final VocabularyService vocabularyService;

  private final AnkiConverterService ankiConverterService;

  private final TemplateCreatorService templateCreatorService;

  private final IOService ioService;

  @Override
  public void export(String sourceLanguage, String targetLanguage, List<String> options) {

    List<AnkiData> ankiDataList = new ArrayList<>();

    List<Vocabulary> vocab = vocabularyService.getAll();

    for (Vocabulary v : vocab) {

      if (counter != 0 && counter % STEP_SIZE == 0) {
        sleep(4000);
      }

      var ankiData = ankiConverterService.convert(v, sourceLanguage, targetLanguage, options);
      ankiDataList.add(ankiData);

      counter++;

    }

    var template = templateCreatorService.create(ankiDataList);

    ioService.print(template);

  }

  private void sleep(long ms) {

    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new KorvoRootException("Interrupted while trying to get data", e);
    }

  }
}
