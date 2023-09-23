package ru.dankoy.korvotoanki.core.service.googletrans;

import java.util.List;

public interface GoogleTranslator {

  String translate(String text, String targetLanguage, String sourceLanguage,
      List<String> dtOptions);
}
