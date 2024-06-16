package ru.dankoy.korvotoanki.core.exceptions;

public class StateDaoException extends KorvoRootException {

  public StateDaoException(Exception e) {
    super(e);
  }

  public StateDaoException(String message) {
    super(message);
  }
}
