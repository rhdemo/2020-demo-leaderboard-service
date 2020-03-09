package com.redhat.developers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import com.redhat.developers.containers.PostgreSqlContainer;
import org.testcontainers.junit.jupiter.Container;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * QuarkusTestEnv
 */
public class QuarkusTestEnv implements QuarkusTestResourceLifecycleManager {

  Logger logger = Logger.getLogger(QuarkusTestEnv.class.getName());

  public final static String JDBC_URL =
      "vertx-reactive:postgresql://%s:%d/gamedb";

  @Container
  public static PostgreSqlContainer postgreSQL = new PostgreSqlContainer();

  @Override
  public Map<String, String> start() {
    postgreSQL.start();
    Map<String, String> sysProps = new HashMap<>();
    // quarkus.datasource.url=vertx-reactive:postgresql://localhost:5432/gamedb
    sysProps.put("quarkus.datasource.url", String.format(JDBC_URL,
        postgreSQL.getContainerIpAddress(), postgreSQL.getMappedPort(5432)));
    return sysProps;
  }

  @Override
  public void stop() {
    postgreSQL.stop();
  }
}
