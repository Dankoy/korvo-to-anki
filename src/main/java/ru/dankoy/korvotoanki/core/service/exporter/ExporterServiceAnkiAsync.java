package ru.dankoy.korvotoanki.core.service.exporter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
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


@ConditionalOnProperty(prefix = "korvo-to-anki", value = "async", havingValue = "true")
@Slf4j
@Service
@RequiredArgsConstructor
public class ExporterServiceAnkiAsync implements ExporterService {

  private static final int STEP_SIZE = 25;
  private static final AtomicInteger atomicInteger = new AtomicInteger(0);
  private static final CountDownLatch latch = new CountDownLatch(2);

  private final VocabularyService vocabularyService;

  private final AnkiConverterService ankiConverterService;

  private final TemplateCreatorService templateCreatorService;

  private final IOService ioService;

  @Override
  public void export(String sourceLanguage, String targetLanguage, List<String> options) {



    List<AnkiData> ankiDataList = new CopyOnWriteArrayList<>();

    ExecutorService executorService = Executors.newFixedThreadPool(2);

    List<Vocabulary> vocab = vocabularyService.getAll();

    List<Vocabulary> oneV = vocab.subList(0, vocab.size() / 2);
    List<Vocabulary> twoV = vocab.subList((vocab.size() / 2), vocab.size());

    executorService.execute(
        () -> asyncFunc(ankiDataList, oneV, sourceLanguage, targetLanguage, options));
    executorService.execute(
        () -> asyncFunc(ankiDataList, twoV, sourceLanguage, targetLanguage, options));

    try {
      latch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new KorvoRootException("Interrupted while waiting for task completion", e);
    }

    var template = templateCreatorService.create(ankiDataList);

    ioService.print(template);

  }

  private void asyncFunc(List<AnkiData> ankiDataList, List<Vocabulary> vocabularies,
      String sourceLanguage, String targetLanguage, List<String> options) {
    for (Vocabulary v : vocabularies) {
      var i = atomicInteger.getAndIncrement();
      if (i != 0 && i % STEP_SIZE == 0) {
        log.info("processed - {}", i);
        log.debug("Sleep {}", Thread.currentThread().getName());
        sleep(6000);
      }
      var ankiData = ankiConverterService.convert(v, sourceLanguage, targetLanguage, options);
      log.info("Thread {} obtained new anki for word {}", Thread.currentThread().getName(),
          ankiData.getWord());
      ankiDataList.add(ankiData);
    }
    latch.countDown();
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
