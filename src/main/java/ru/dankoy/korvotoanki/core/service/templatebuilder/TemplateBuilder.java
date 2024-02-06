package ru.dankoy.korvotoanki.core.service.templatebuilder;

import java.util.Map;

public interface TemplateBuilder {

  String writeTemplate(Map<String, Object> templateData, String templateName);

  String loadTemplateFromString(
      String templateName, String templateString, Map<String, Object> templateData);
}
