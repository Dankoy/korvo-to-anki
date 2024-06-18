package ru.dankoy.korvotoanki.core.domain.state;

import java.time.LocalDateTime;

public record State(long id, String word, LocalDateTime created) {

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof State state)) return false;

    return word.equals(state.word) && created.equals(state.created);
  }

  @Override
  public int hashCode() {
    int result = word.hashCode();
    result = 31 * result + created.hashCode();
    return result;
  }
}
