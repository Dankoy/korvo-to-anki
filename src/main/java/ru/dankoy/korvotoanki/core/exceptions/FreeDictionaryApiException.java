package ru.dankoy.korvotoanki.core.exceptions;

public class FreeDictionaryApiException extends KorvoRootException {

  public FreeDictionaryApiException(Exception e) {
    super(e);
  }

  public FreeDictionaryApiException(String message, Exception e) {
    super(message, e);
  }

  public FreeDictionaryApiException(String message) {
    super(message);
  }
}
