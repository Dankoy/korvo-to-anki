package ru.dankoy.korvotoanki.core.service.exporter;

import java.util.List;

public interface ExporterService {

  void export(String sourceLanguage, String targetLanguage, List<String> options);

}
