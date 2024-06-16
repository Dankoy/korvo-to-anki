package ru.dankoy.korvotoanki.migration.flyway;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.stereotype.Component;
import ru.dankoy.korvotoanki.core.domain.state.State;
import ru.dankoy.korvotoanki.core.service.state.StateService;

/** Migrate from state file with json data to sqlite database if file exists */
@RequiredArgsConstructor
@Component
@Slf4j
public class V2__MigrateFileStateToSqlite extends BaseJavaMigration {

  private static final int BATCH_SIZE = 100;

  private final StateService fileStateService;

  @Override
  public void migrate(Context context) throws Exception {

    List<State> currentState = fileStateService.checkState();

    if (!currentState.isEmpty()) {

      Collection<List<State>> batches =
          currentState.stream()
              .collect(Collectors.groupingBy(it -> currentState.indexOf(it) / BATCH_SIZE))
              .values();

      var connection = context.getConnection();

      connection.setAutoCommit(false);

      try (var ps = connection.prepareStatement("insert into state(word) values (?)")) {

        for (List<State> batch : batches) {

          for (State state : batch) {

            ps.setString(1, state.word());
            ps.addBatch();
          }

          // batch every 100 elements
          int[] numUpdates = ps.executeBatch();

          for (int i = 0; i < numUpdates.length; i++) {
            if (numUpdates[i] == -2) log.error("Execution {}: unknown number of rows updated", i);
            else log.debug("Execution {} successful: {} rows updated", i, numUpdates[i]);
          }
        }

        connection.commit();
      }
    } else {
      log.info("No state in file to migrate from");
    }
  }
}
