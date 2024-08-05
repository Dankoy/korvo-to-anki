package ru.dankoy.korvotoanki.core.service.exporter;

import java.util.ArrayList;
import java.util.List;
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

/**
 * @deprecated in favor of {@link ExporterServiceAnkiAsync}
 */

@Deprecated(since = "2024-08-05", forRemoval = true)
@Slf4j
@ConditionalOnProperty(prefix = "korvo-to-anki", value = "async", havingValue = "false")
@Service
@RequiredArgsConstructor
public class ExporterServiceAnki implements ExporterService {

  private static final int STEP_SIZE = 10;
  private final VocabularyService vocabularyService;
  private final AnkiConverterService ankiConverterService;
  private final TemplateCreatorService templateCreatorService;
  private final FilesProperties filesProperties;
  private final StateService sqliteStateService;
  private int counter = 0;

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

    List<AnkiData> ankiDataList = new ArrayList<>();

    List<Vocabulary> vocabulariesFull = vocabularyService.getAll();

    List<Vocabulary> filtered = sqliteStateService.filterState(vocabulariesFull);

    if (!filtered.isEmpty()) {
      for (Vocabulary v : filtered) {

        if (counter != 0 && counter % STEP_SIZE == 0) {
          sleep(4000);
        }

        var ankiData = ankiConverterService.convert(v, sourceLanguage, targetLanguage, options);
        ankiDataList.add(ankiData);

        counter++;
      }

      var template = templateCreatorService.create(ankiDataList);

      var ioService =
          getIoService(
              getFileProviderService(),
              getFileNameFormatterService(),
              filesProperties.getExportFileName());

      ioService.print(template);
      sqliteStateService.saveState(vocabulariesFull);

    } else {
      log.info("State is the same as database. Export is not necessary.");
    }
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
