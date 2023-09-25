package ru.dankoy.korvotoanki.core.service.googletrans.parser;

import ru.dankoy.korvotoanki.core.domain.googletranslation.GoogleTranslation;

public interface GoogleTranslatorParser {

  GoogleTranslation parse(String data);
}
