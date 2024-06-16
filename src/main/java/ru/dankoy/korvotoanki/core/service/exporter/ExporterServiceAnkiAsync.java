package ru.dankoy.korvotoanki.core.service.exporter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.dankoy.korvotoanki.config.appprops.FilesProperties;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;
import ru.dankoy.korvotoanki.core.exceptions.KorvoRootException;
import ru.dankoy.korvotoanki.core.service.converter.AnkiConverterService;
import ru.dankoy.korvotoanki.core.service.filenameformatter.FileNameFormatterService;
import ru.dankoy.korvotoanki.core.service.fileprovider.FileProviderService;
import ru.dankoy.korvotoanki.core.service.io.IOService;
import ru.dankoy.korvotoanki.core.service.state.StateService;
import ru.dankoy.korvotoanki.core.service.templatecreator.TemplateCreatorService;
import ru.dankoy.korvotoanki.core.service.vocabulary.VocabularyService;

@ConditionalOnProperty(prefix = "korvo-to-anki", value = "async", havingValue = "true")
@Slf4j
@Service
@RequiredArgsConstructor
public class ExporterServiceAnkiAsync implements ExporterService {

  private static final int STEP_SIZE = 30;
  private static final int THREADS = 2;
  private static final AtomicInteger atomicInteger = new AtomicInteger(0);
  private final VocabularyService vocabularyService;
  private final AnkiConverterService ankiConverterService;
  private final TemplateCreatorService templateCreatorService;
  private final FilesProperties filesProperties;
  private final StateService sqliteStateService;
  private CountDownLatch latch;

  // The IoService is provided type, that's why we inject it using @Lookup annotation.
  // @Lookup annotation doesn't work inside prototype bean, so had to use constructor to inject
  // beans
  @Lookup
  public IOService getIoService(
      FileProviderService fileProviderService,
      FileNameFormatterService fileNameFormatterService,
      String fileName) {
    return null;
  }

  @Lookup
  public FileProviderService getFileProviderService() {
    return null;
  }

  @Lookup
  public FileNameFormatterService getFileNameFormatterService() {
    return null;
  }

  @Override
  public void export(String sourceLanguage, String targetLanguage, List<String> options) {

    List<AnkiData> ankiDataList = new CopyOnWriteArrayList<>();

    try (ExecutorService executorService = Executors.newFixedThreadPool(THREADS)) {
      List<Vocabulary> vocabulariesFull = vocabularyService.getAll();
      List<Vocabulary> filtered = sqliteStateService.filterState(vocabulariesFull);

      if (!filtered.isEmpty()) {

        if (filtered.size() < THREADS) {

          latch = new CountDownLatch(filtered.size());
          executorService.execute(
              () -> asyncFunc(ankiDataList, filtered, sourceLanguage, targetLanguage, options));

        } else {

          latch = new CountDownLatch(THREADS);
          List<Vocabulary> oneV = filtered.subList(0, filtered.size() / 2);
          List<Vocabulary> twoV = filtered.subList((filtered.size() / 2), filtered.size());

          executorService.execute(
              () -> asyncFunc(ankiDataList, oneV, sourceLanguage, targetLanguage, options));
          executorService.execute(
              () -> asyncFunc(ankiDataList, twoV, sourceLanguage, targetLanguage, options));
        }

        try {
          latch.await();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new KorvoRootException("Interrupted while waiting for task completion", e);
        }

        var template = templateCreatorService.create(ankiDataList);

        var ioService =
            getIoService(
                getFileProviderService(),
                getFileNameFormatterService(),
                filesProperties.getExportFileName());
        ioService.print(template);

        sqliteStateService.saveState(filtered);

      } else {
        log.info("State is the same as database. Export is not necessary.");
      }
    }
  }

  private void asyncFunc(
      List<AnkiData> ankiDataList,
      List<Vocabulary> vocabularies,
      String sourceLanguage,
      String targetLanguage,
      List<String> options) {
    for (Vocabulary v : vocabularies) {
      var i = atomicInteger.getAndIncrement();
      if (i != 0 && i % STEP_SIZE == 0) {
        log.info("processed - {}", i);
        log.debug("Sleep {}", Thread.currentThread().getName());
        sleep(10000);
      }
      var ankiData = ankiConverterService.convert(v, sourceLanguage, targetLanguage, options);
      log.info(
          "Thread {} obtained new anki for word {}",
          Thread.currentThread().getName(),
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
