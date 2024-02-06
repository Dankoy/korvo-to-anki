package ru.dankoy.korvotoanki.core.domain;

public record Vocabulary(
    String word,
    Title title,
    long createTime,
    long reviewTime,
    long dueTime,
    long reviewCount,
    String prevContext,
    String nextContext,
    long streakCount) {}
