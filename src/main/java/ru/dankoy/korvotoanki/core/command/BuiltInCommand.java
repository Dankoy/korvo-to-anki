package ru.dankoy.korvotoanki.core.command;

import org.springframework.shell.command.annotation.Command;
import ru.dankoy.korvotoanki.config.ThreadPoolConfig;
import ru.dankoy.korvotoanki.core.service.converter.AnkiConverterServiceCompletableFuture;

@Command(group = "Built-In Commands")
public class BuiltInCommand {

  /**
   * Shutdown shell gracefully. Since {@link
   * AnkiConverterServiceCompletableFuture#convert(ru.dankoy.korvotoanki.core.domain.Vocabulary,
   * String, String, java.util.List)} is a completable future-based service, it needs a normal
   * thread pool instead of ForkJoinPool. This pool is created as bean in {@link
   * ThreadPoolConfig#ankiConverterTaskExecutor()}
   */
  @Command(command = "quit", alias = "exit", description = "Shutdown shell gracefully")
  public void quit() {
    // Still graceful shutdown like SIGTERM
    System.exit(0);
  }
}
