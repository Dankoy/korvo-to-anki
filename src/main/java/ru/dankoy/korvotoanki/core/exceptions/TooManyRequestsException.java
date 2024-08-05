package ru.dankoy.korvotoanki.core.exceptions;

public class TooManyRequestsException extends DictionaryApiException {

  public TooManyRequestsException(String message) {
    super(message);
  }
}
