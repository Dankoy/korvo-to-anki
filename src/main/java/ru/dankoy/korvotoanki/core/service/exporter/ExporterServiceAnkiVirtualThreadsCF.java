package ru.dankoy.korvotoanki.core.service.exporter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import ru.dankoy.korvotoanki.config.appprops.FilesProperties;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;
import ru.dankoy.korvotoanki.core.service.converter.AnkiConverterService;
import ru.dankoy.korvotoanki.core.service.io.IOService;
import ru.dankoy.korvotoanki.core.service.state.StateService;
import ru.dankoy.korvotoanki.core.service.templatecreator.TemplateCreatorService;
import ru.dankoy.korvotoanki.core.service.vocabulary.VocabularyService;

@ConditionalOnExpression("${korvo-to-anki.async} and '${korvo-to-anki.async-type}'.equals('vtcf')")
@Slf4j
@Service
@RequiredArgsConstructor
public class ExporterServiceAnkiVirtualThreadsCF implements ExporterService {

  private static final int STEP_SIZE = 30;
  private static final AtomicInteger atomicInteger = new AtomicInteger(0);
  private final VocabularyService vocabularyService;
  private final AnkiConverterService ankiConverterService;
  private final TemplateCreatorService templateCreatorService;
  private final FilesProperties filesProperties;
  private final StateService sqliteStateService;
  private final Function<String, IOService> ioServiceFileFactory;

  @Override
  public void export(String sourceLanguage, String targetLanguage, List<String> options) {

    List<AnkiData> ankiDataList = new CopyOnWriteArrayList<>();

    List<Vocabulary> vocabulariesFull = vocabularyService.getAll();
    List<Vocabulary> filtered = sqliteStateService.filterState(vocabulariesFull);

    var virtualThreadsExecutorService = Executors.newVirtualThreadPerTaskExecutor();

    if (!filtered.isEmpty()) {

      CompletableFuture<Void> concurrentExportAllOf = null;

      List<CompletableFuture<AnkiData>> completableFutures = new ArrayList<>();

      for (Vocabulary vocabulary : filtered) {

        completableFutures.add(
            CompletableFuture.supplyAsync(
                    () -> asyncFunc(vocabulary, sourceLanguage, targetLanguage, options),
                    virtualThreadsExecutorService)
                .whenCompleteAsync(
                    (res, ex) -> {
                      if (ex == null) {

                        ankiDataList.add(res);
                        var i = atomicInteger.getAndIncrement();
                        if (i != 0 && i % STEP_SIZE == 0) {
                          log.info("processed - {}", i);
                        }

                      } else {
                        log.error(
                            "Something went wrong with word: {}\n{}", vocabulary, ex.getMessage());
                        throw new CompletionException(ex);
                      }
                    }));
      }

      concurrentExportAllOf =
          CompletableFuture.allOf(
              completableFutures.toArray(new CompletableFuture[completableFutures.size()]));

      // wait till export is finished
      concurrentExportAllOf.join();

      var ioService = ioServiceFileFactory.apply(filesProperties.getExportFileName());

      // prepare cf for printing the template
      CompletableFuture<String> template =
          CompletableFuture.supplyAsync(
              () -> templateCreatorService.create(ankiDataList), virtualThreadsExecutorService);

      // run all of the futures in parallel
      CompletableFuture<Void> printExportAndSaveState =
          CompletableFuture.allOf(
              CompletableFuture.runAsync(
                  () -> ioService.print(template.join()), virtualThreadsExecutorService),
              CompletableFuture.runAsync(
                  () -> sqliteStateService.saveState(filtered), virtualThreadsExecutorService));

      // wait till done
      printExportAndSaveState
          .whenComplete((e, ex) -> virtualThreadsExecutorService.shutdownNow())
          .join();

    } else {
      log.info("State is the same as database. Export is not necessary.");
    }
  }

  private AnkiData asyncFunc(
      Vocabulary vocabulary, String sourceLanguage, String targetLanguage, List<String> options) {
    var ankiData =
        ankiConverterService.convert(vocabulary, sourceLanguage, targetLanguage, options);
    log.info(
        "Thread {} obtained new anki for word {}",
        Thread.currentThread().toString(),
        ankiData.getWord());
    return ankiData;
  }
}
