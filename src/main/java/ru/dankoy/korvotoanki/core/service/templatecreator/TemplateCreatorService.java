package ru.dankoy.korvotoanki.core.service.templatecreator;

import java.util.List;
import ru.dankoy.korvotoanki.core.domain.anki.AnkiData;

public interface TemplateCreatorService {

  String create(List<AnkiData> ankiDataList);

}
