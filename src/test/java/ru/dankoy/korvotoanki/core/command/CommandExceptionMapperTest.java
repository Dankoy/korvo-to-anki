package ru.dankoy.korvotoanki.core.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.shell.core.command.ExitStatus;
import ru.dankoy.korvotoanki.core.exceptions.KorvoRootException;

class CommandExceptionMapperTest {

  private final CommandExceptionMapper mapper = new CommandExceptionMapper();

  @Test
  @DisplayName("unwraps InvocationTargetException and returns root cause message")
  void returnsRootCauseMessage() {
    var root = new KorvoRootException("Books not found in database: [Nope]");
    var wrapped = new InvocationTargetException(root);

    ExitStatus status = mapper.apply(wrapped);

    assertEquals(ExitStatus.EXECUTION_ERROR.code(), status.code());
    assertEquals("Books not found in database: [Nope]", status.description());
  }

  @Test
  @DisplayName("passes through plain exceptions with their message")
  void returnsPlainMessage() {
    ExitStatus status = mapper.apply(new IllegalStateException("plain failure"));

    assertEquals("plain failure", status.description());
  }

  @Test
  @DisplayName("falls back to simple class name when message is null")
  void fallsBackToClassName() {
    ExitStatus status = mapper.apply(new NullPointerException());

    assertTrue(status.description().contains("NullPointerException"));
  }
}
