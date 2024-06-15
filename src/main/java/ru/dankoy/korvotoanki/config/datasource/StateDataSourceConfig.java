package ru.dankoy.korvotoanki.config.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.migration.JavaMigration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
// @EnableJdbcRepositories(
//    basePackages = "ru.dankoy.korvotoanki.core.dao.state",
//    transactionManagerRef = "stateTransactionManager",
//    jdbcOperationsRef = "vocabularyJdbcOperations")
@RequiredArgsConstructor
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class StateDataSourceConfig {

  private final ApplicationContext
      applicationContext; // obtain a reference to Spring's ApplicationContext.

  @Bean
  @ConfigurationProperties("spring.datasource.state")
  public DataSourceProperties stateDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @ConfigurationProperties(prefix = "spring.datasource.state.hikari")
  public HikariConfig stateHikariConfig() {
    return new HikariConfig();
  }

  //  @Primary
  //  @FlywayDataSource
  //  @Bean
  private DataSource stateFLywayDataSource(DataSourceProperties stateDataSourceProperties) {
    return DataSourceBuilder.create()
        .driverClassName(stateDataSourceProperties.getDriverClassName())
        .url(stateDataSourceProperties.getUrl())
        .build();
  }

  @Primary
  @Bean
  public DataSource stateDataSource() throws SQLException {
    migrateFlyway(stateFlywayProperties());
    return new HikariDataSource(stateHikariConfig());
  }

  @Bean
  @ConfigurationProperties(prefix = "spring.datasource.state.flyway")
  public FlywayProperties stateFlywayProperties() {
    return new FlywayProperties();
  }

  private void migrateFlyway(@Qualifier("stateFlywayProperties") FlywayProperties flywayProperties)
      throws SQLException {

    var ds = stateFLywayDataSource(stateDataSourceProperties());

    var f =
        Flyway.configure()
            .dataSource(ds)
            .schemas(flywayProperties.getSchemas().toArray(new String[0]))
            .locations(flywayProperties.getLocations().toArray(new String[0]))
            .javaMigrations(
                applicationContext
                    .getBeansOfType(JavaMigration.class)
                    .values()
                    .toArray(new JavaMigration[0]))
            .load()
            .migrate();

    if (f.success) {
      ds.getConnection().close();
      log.info("Closed flyway connection");
    }
  }
}
