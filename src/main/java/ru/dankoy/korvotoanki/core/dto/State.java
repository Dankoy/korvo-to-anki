package ru.dankoy.korvotoanki.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @deprecated use {@link State} instead
 */
@Deprecated(since = "2024-06-15")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class State {

  private String word;
}
