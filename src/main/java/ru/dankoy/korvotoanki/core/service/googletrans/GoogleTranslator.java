package ru.dankoy.korvotoanki.core.service.googletrans;

import java.util.List;

/*
* Used info from:
* - https://github.com/koreader/koreader/blob/34ba2fab301f43533ee6876d78809f685dd05614/frontend/ui/translator.lua#L4
* - https://koreader.rocks/doc/modules/ui.translator.html
* - https://github.com/ssut/py-googletrans/blob/master/googletrans/client.py
* */

public interface GoogleTranslator {

  String translate(String text, String targetLanguage, String sourceLanguage,
      List<String> dtOptions);
}
