package ru.dankoy.korvotoanki.core.domain.state;

import java.time.LocalDateTime;

public record State(long id, String word, LocalDateTime created) {}
