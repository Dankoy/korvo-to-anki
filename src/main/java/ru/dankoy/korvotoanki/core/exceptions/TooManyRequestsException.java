package ru.dankoy.korvotoanki.core.exceptions;

public class TooManyRequestsException extends RuntimeException {

  public TooManyRequestsException(String message) {
    super(message);
  }
}
