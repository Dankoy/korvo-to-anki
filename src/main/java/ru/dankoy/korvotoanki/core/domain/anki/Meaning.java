package ru.dankoy.korvotoanki.core.domain.anki;

import java.util.List;

/**
 * @param type        noun, verb
 * @param definitions actual definitions
 * @param synonyms
 * @param antonyms
 */
public record Meaning(
    String type,
    List<Definition> definitions,
    List<String> synonyms,
    List<String> antonyms) {

}
