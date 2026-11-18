package ru.dankoy.korvotoanki.core.domain.googletranslation;

/**
 * @param type noun, verb, exclamation, etc
 * @param info the actual definition
 * @param example example sentence for the definition, could be null
 * @param definitionId internal google translate dictionary id of the definition. The same id is
 *     used in synonyms to link them to the definition
 */
public record Definition(String type, String info, String example, String definitionId) {}
