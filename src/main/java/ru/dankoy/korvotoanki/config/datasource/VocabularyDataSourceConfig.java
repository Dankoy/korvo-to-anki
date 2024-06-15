package ru.dankoy.korvotoanki.config.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
// @EnableJdbcRepositories(
//    basePackages = "ru.dankoy.korvotoanki.core.dao.vocabularybuilder",
//    transactionManagerRef = "vocabularyTransactionManager",
//    jdbcOperationsRef = "vocabularyJdbcOperations")
@EnableAutoConfiguration(
    exclude = {DataSourceAutoConfiguration.class, LiquibaseAutoConfiguration.class})
public class VocabularyDataSourceConfig {

  @Bean
  @ConfigurationProperties("spring.datasource")
  public DataSourceProperties vocabularyDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @ConfigurationProperties(prefix = "spring.datasource.hikari")
  public HikariConfig vocabularyHikariConfig() {
    return new HikariConfig();
  }

  @Bean
  public DataSource vocabularyDataSource() {
    return new HikariDataSource(vocabularyHikariConfig());
  }

  @Bean(name = "vocabularyTransactionManager")
  public PlatformTransactionManager vocabularyTransactionManager(
      @Qualifier("vocabularyDataSource") DataSource vocabularyDataSource) {
    return new JdbcTransactionManager(vocabularyDataSource);
  }

  @Bean(name = "vocabularyJdbcOperations")
  public NamedParameterJdbcOperations vocabularyJdbcOperations(
      @Qualifier("vocabularyDataSource") DataSource dataSource) {
    return new NamedParameterJdbcTemplate(dataSource);
  }
}
