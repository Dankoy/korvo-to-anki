package ru.dankoy.korvotoanki.core.exceptions;

public class KorvoRootException extends RuntimeException {

  public KorvoRootException(String message, Exception e) {
    super(message, e);
  }

  public KorvoRootException(Exception e) {
    super(e);
  }

  public KorvoRootException(String message) {
    super(message);
  }
}
