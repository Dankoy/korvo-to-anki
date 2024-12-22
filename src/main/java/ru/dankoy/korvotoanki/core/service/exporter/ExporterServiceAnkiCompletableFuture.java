package ru.dankoy.korvotoanki.core.service.exporter;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import ru.dankoy.korvotoanki.config.appprops.FilesProperties;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;
import ru.dankoy.korvotoanki.core.service.converter.AnkiConverterService;
import ru.dankoy.korvotoanki.core.service.filenameformatter.FileNameFormatterService;
import ru.dankoy.korvotoanki.core.service.fileprovider.FileProviderService;
import ru.dankoy.korvotoanki.core.service.io.IOService;
import ru.dankoy.korvotoanki.core.service.state.StateService;
import ru.dankoy.korvotoanki.core.service.templatecreator.TemplateCreatorService;
import ru.dankoy.korvotoanki.core.service.vocabulary.VocabularyService;

@ConditionalOnExpression(
    "${korvo-to-anki.async} and '${korvo-to-anki.async-type}'.equals('completable_future')")
@Slf4j
@Service
@RequiredArgsConstructor
public class ExporterServiceAnkiCompletableFuture implements ExporterService {

  private static final int STEP_SIZE = 30;
  private static final int THREADS = 2;
  private static final AtomicInteger atomicInteger = new AtomicInteger(0);
  private final VocabularyService vocabularyService;
  private final AnkiConverterService ankiConverterService;
  private final TemplateCreatorService templateCreatorService;
  private final FilesProperties filesProperties;
  private final StateService sqliteStateService;

  // The IoService is provided type, that's why we inject it using @Lookup
  // annotation.
  // @Lookup annotation doesn't work inside prototype bean, so had to use
  // constructor to inject
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

    List<Vocabulary> vocabulariesFull = vocabularyService.getAll();
    List<Vocabulary> filtered = sqliteStateService.filterState(vocabulariesFull);

    var fixedThreadPool = Executors.newFixedThreadPool(THREADS);

    if (!filtered.isEmpty()) {

      CompletableFuture<Void> concurrentExportAllOf = null;

      if (filtered.size() < THREADS) {

        CompletableFuture<Void> future1 =
            CompletableFuture.runAsync(
                () -> asyncFunc(ankiDataList, filtered, sourceLanguage, targetLanguage, options),
                fixedThreadPool);

        concurrentExportAllOf = CompletableFuture.allOf(future1);

      } else {

        List<Vocabulary> oneV = filtered.subList(0, filtered.size() / 2);
        List<Vocabulary> twoV = filtered.subList((filtered.size() / 2), filtered.size());

        CompletableFuture<Void> future1 =
            CompletableFuture.runAsync(
                () -> asyncFunc(ankiDataList, oneV, sourceLanguage, targetLanguage, options),
                fixedThreadPool);
        CompletableFuture<Void> future2 =
            CompletableFuture.runAsync(
                () -> asyncFunc(ankiDataList, twoV, sourceLanguage, targetLanguage, options),
                fixedThreadPool);
        concurrentExportAllOf = CompletableFuture.allOf(future1, future2);
      }

      // wait till export is finished
      concurrentExportAllOf.join();

      var ioService =
          getIoService(
              getFileProviderService(),
              getFileNameFormatterService(),
              filesProperties.getExportFileName());

      // prepare cf for printing the template
      CompletableFuture<String> template =
          CompletableFuture.supplyAsync(
              () -> templateCreatorService.create(ankiDataList), fixedThreadPool);

      // run all of the futures in parallel
      CompletableFuture<Void> printExportAndSaveState =
          CompletableFuture.allOf(
              CompletableFuture.runAsync(() -> ioService.print(template.join()), fixedThreadPool),
              CompletableFuture.runAsync(
                  () -> sqliteStateService.saveState(filtered), fixedThreadPool));

      // wait till done
      printExportAndSaveState.whenComplete((e, ex) -> fixedThreadPool.shutdownNow()).join();

    } else {
      log.info("State is the same as database. Export is not necessary.");
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
      // sleep is not necessary anymore since rate limiter for dictionary api is
      // implemented
      if (i != 0 && i % STEP_SIZE == 0) {
        log.info("processed - {}", i);
      }
      var ankiData = ankiConverterService.convert(v, sourceLanguage, targetLanguage, options);
      log.info(
          "Thread {} obtained new anki for word {}",
          Thread.currentThread().getName(),
          ankiData.getWord());
      ankiDataList.add(ankiData);
    }
  }
}
