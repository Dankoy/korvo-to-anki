package ru.dankoy.korvotoanki.core.exceptions;

public class GoogleTranslatorException extends KorvoRootException {

  public GoogleTranslatorException(Exception e) {
    super(e);
  }

  public GoogleTranslatorException(String message, Exception e) {
    super(message, e);
  }
}
