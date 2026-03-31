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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;
import ru.dankoy.korvotoanki.core.dto.AnkiDataDTO;
import ru.dankoy.korvotoanki.core.exceptions.KorvoRootException;
import ru.dankoy.korvotoanki.core.service.templatebuilder.TemplateBuilder;

@Slf4j
@Service
public class TemplateCreatorServiceImpl implements TemplateCreatorService {

  private final String appName;

  private final TemplateBuilder templateBuilder;
  private CountDownLatch latch;

  public TemplateCreatorServiceImpl(
      @Value("${spring.application.name}") String appName, TemplateBuilder templateBuilder) {
    this.appName = appName;
    this.templateBuilder = templateBuilder;
  }

  @Override
  public String create(List<AnkiData> ankiDataList) {

    List<AnkiDataDTO> dtos = new CopyOnWriteArrayList<>();
    Map<String, Object> templateDataFull = new HashMap<>();

    // async with splitting list into chunks
    int cores = Runtime.getRuntime().availableProcessors();
    List<List<AnkiData>> split = splitToPartitions(ankiDataList, cores);

    try (ExecutorService executorService = Executors.newFixedThreadPool(cores)) {
      latch = new CountDownLatch(split.size());

      for (List<AnkiData> sp : split) {
        executorService.execute(() -> convertToDto(dtos, sp));
      }

      try {
        latch.await();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new KorvoRootException("Interrupted while waiting for task completion", e);
      }
    }

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

      dto.setTags(dto.getTags().stream().map(t -> appName + "::" + t).toList());

      dtos.add(dto);
    }

    latch.countDown();
  }
}
