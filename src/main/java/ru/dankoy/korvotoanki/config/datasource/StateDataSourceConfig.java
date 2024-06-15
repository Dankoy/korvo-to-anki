package ru.dankoy.korvotoanki.config.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
// @EnableJdbcRepositories(
//    basePackages = "ru.dankoy.korvotoanki.core.dao.state",
//    transactionManagerRef = "stateTransactionManager",
//    jdbcOperationsRef = "vocabularyJdbcOperations")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class StateDataSourceConfig {

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

  @Bean
  public DataSource stateDataSource() {
    return new HikariDataSource(stateHikariConfig());
  }

  @Bean
  @ConfigurationProperties(prefix = "spring.datasource.state.liquibase")
  public LiquibaseProperties stateLiquibaseProperties() {
    return new LiquibaseProperties();
  }

  @Bean
  public SpringLiquibase stateLiquibase() {
    return springLiquibase(stateDataSource(), stateLiquibaseProperties());
  }

  private static SpringLiquibase springLiquibase(
      DataSource dataSource, LiquibaseProperties properties) {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog(properties.getChangeLog());
    liquibase.setContexts(properties.getContexts());
    liquibase.setDefaultSchema(properties.getDefaultSchema());
    liquibase.setDropFirst(properties.isDropFirst());
    liquibase.setShouldRun(properties.isEnabled());
    liquibase.setLabelFilter(properties.getLabelFilter());
    liquibase.setChangeLogParameters(properties.getParameters());
    liquibase.setRollbackFile(properties.getRollbackFile());
    return liquibase;
  }
}
