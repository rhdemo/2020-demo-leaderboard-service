package com.redhat.developers.containers;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * PostgreSqlContainer
 */
public class PostgreSqlContainer extends PostgreSQLContainer {

  public PostgreSqlContainer() {
    withInitScript("import.sql");
    withDatabaseName("gamedb");
    withUsername("demo");
    withPassword("password!");
    waitingFor(Wait.forListeningPort());
  }

  @Override
  public void start() {
    addFixedExposedPort(5432, 5432);
    super.start();
  }

}
