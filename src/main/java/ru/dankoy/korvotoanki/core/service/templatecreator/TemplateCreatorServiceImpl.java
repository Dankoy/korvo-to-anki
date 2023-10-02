package ru.dankoy.korvotoanki.core.service.templatecreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;
import ru.dankoy.korvotoanki.core.dto.AnkiDataDTO;
import ru.dankoy.korvotoanki.core.exceptions.KorvoRootException;
import ru.dankoy.korvotoanki.core.service.templatebuilder.TemplateBuilder;


@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateCreatorServiceImpl implements TemplateCreatorService {

  private final TemplateBuilder templateBuilder;
  private CountDownLatch latch = new CountDownLatch(
      Runtime.getRuntime().availableProcessors());

  @Override
  public String create(List<AnkiData> ankiDataList) {

    List<AnkiDataDTO> dtos = new CopyOnWriteArrayList<>();
    Map<String, Object> templateDataFull = new HashMap<>();

    // async with splitting list into chunks
    int cores = Runtime.getRuntime().availableProcessors();
    List<List<AnkiData>> splitted = splitToPartitions(ankiDataList, cores);
    ExecutorService executorService = Executors.newFixedThreadPool(cores);

    int toLatch = 0;
    for (List<AnkiData> sp : splitted) {
      executorService.execute(() -> convertToDto(dtos, sp));
      toLatch++;
    }

    toLatch = Math.min(toLatch, Runtime.getRuntime().availableProcessors());
    latch = new CountDownLatch(toLatch);

    try {
      latch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new KorvoRootException("Interrupted while waiting for task completion", e);
    }

    executorService.shutdown();

    // create full template
    templateDataFull.put("ankiDataList", dtos);

    return templateBuilder.writeTemplate(templateDataFull, "korvo-to-anki.ftl");
  }


  private List<List<AnkiData>> splitToPartitions(List<AnkiData> list, int cores) {

    if (list.size() < cores) {
      List<List<AnkiData>> l = new ArrayList<>();
      l.add(list);
      return l;
    }

    final int G = list.size() / cores; // chunks size
    final int NG = (list.size() + G - 1) / G;

    return IntStream.range(0, NG)
        .mapToObj(i -> list.subList(G * i, Math.min(G * i + G, list.size())))
        .toList();

  }

  private void convertToDto(List<AnkiDataDTO> dtos, List<AnkiData> ankiDataList) {

    Map<String, Object> templateDataMeaning = new HashMap<>();

    for (AnkiData ankiData : ankiDataList) {

      // Use dto
      AnkiDataDTO dto = AnkiDataDTO.toDTO(ankiData);

      // create meaning string from template
      templateDataMeaning.put("ankiData", ankiData);
      var meaningString = templateBuilder.writeTemplate(templateDataMeaning, "meaning.ftl");

      dto.setMeanings(meaningString.replace("\t", ""));
      dto.setMeanings(meaningString.replace("\n", ""));

      dtos.add(dto);

    }

    latch.countDown();

  }

}
