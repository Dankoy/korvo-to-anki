package ru.dankoy.korvotoanki.core.domain.anki;

import java.util.List;

/**
 * @param info the actual definition
 * @param example example sentence for the definition, could be null
 * @param synonyms synonyms of the definition, could be empty
 * @param antonyms antonyms of the definition, could be empty
 */
public record Definition(
    String info, String example, List<String> synonyms, List<String> antonyms) {}
