package ru.dankoy.korvotoanki.core.domain.googletranslation;

/**
 * @param type noun, verb, exclamation, etc
 * @param info the actual definition
 */
public record Definition(String type, String info) {}
