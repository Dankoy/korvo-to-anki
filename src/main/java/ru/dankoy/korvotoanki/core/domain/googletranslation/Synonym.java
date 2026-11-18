package ru.dankoy.korvotoanki.core.domain.googletranslation;

import java.util.List;

/**
 * Synonyms of one sense group. Sense group is bound to definition by definitionId.
 *
 * @param type noun, verb, exclamation, etc
 * @param definitionId internal google translate dictionary id of the bound definition. Could be
 *     null or empty when there is no bound definition
 * @param synonyms list of synonyms of source text for the sense group
 */
public record Synonym(String type, String definitionId, List<String> synonyms) {}
