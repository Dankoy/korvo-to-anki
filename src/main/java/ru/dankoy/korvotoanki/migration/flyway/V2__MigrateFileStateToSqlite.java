package ru.dankoy.korvotoanki.migration.flyway;

import java.sql.PreparedStatement;
import java.util.List;
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

  private final StateService fileStateService;

  @Override
  public void migrate(Context context) throws Exception {

    List<State> currentState = fileStateService.checkState();

    if (!currentState.isEmpty()) {

      var connection = context.getConnection();

      connection.setAutoCommit(false);

      try (PreparedStatement ps =
          connection.prepareStatement("insert into state(word) values (?)")) {

        int count = 0;

        for (State elem : currentState) {

          ps.setString(1, elem.word());
          ps.addBatch();

          count++;

          // batch every 100 elements
          if (count % 100 == 0) {
            int[] numUpdates = ps.executeBatch();

            for (int i = 0; i < numUpdates.length; i++) {
              if (numUpdates[i] == -2) log.error("Execution {}: unknown number of rows updated", i);
              else log.debug("Execution {} successful: {} rows updated", i, numUpdates[i]);
            }
          }

          connection.commit();
        }
      }
    }
  }
}
