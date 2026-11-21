package ru.dankoy.korvotoanki.core.command;

import java.lang.reflect.InvocationTargetException;
import org.springframework.shell.core.command.ExitStatus;
import org.springframework.shell.core.command.exit.ExitStatusExceptionMapper;
import org.springframework.stereotype.Component;

/**
 * Maps exceptions thrown by command methods to {@link ExitStatus} so the shell prints the real
 * error message instead of the reflection wrapper's null message ("Unable to run command ...:
 * null").
 */
@Component("korvoExitStatusExceptionMapper")
public class CommandExceptionMapper implements ExitStatusExceptionMapper {

  @Override
  public ExitStatus apply(Exception e) {
    Throwable cause = e instanceof InvocationTargetException ite ? ite.getTargetException() : e;
    String message =
        cause != null && cause.getMessage() != null
            ? cause.getMessage()
            : cause.getClass().getSimpleName();
    return new ExitStatus(ExitStatus.EXECUTION_ERROR.code(), message);
  }
}
