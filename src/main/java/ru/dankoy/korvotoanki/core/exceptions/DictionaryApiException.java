package ru.dankoy.korvotoanki.core.exceptions;

public class DictionaryApiException extends KorvoRootException {

  public DictionaryApiException(Exception e) {
    super(e);
  }

  public DictionaryApiException(String message, Exception e) {
    super(message, e);
  }

  public DictionaryApiException(String message) {
    super(message);
  }
}
