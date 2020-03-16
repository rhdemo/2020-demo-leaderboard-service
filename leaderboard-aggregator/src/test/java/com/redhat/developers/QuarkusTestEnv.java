package com.redhat.developers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import com.redhat.developers.containers.PostgreSqlContainer;
import com.redhat.developers.containers.QdrouterContainer;
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

  @Container
  public static QdrouterContainer amqpContainer = new QdrouterContainer();

  @Override
  public Map<String, String> start() {
    amqpContainer.start();
    postgreSQL.start();
    Map<String, String> sysProps = new HashMap<>();
    // quarkus.datasource.url=vertx-reactive:postgresql://localhost:5432/gamedb
    sysProps.put("quarkus.datasource.url", String.format(JDBC_URL,
        postgreSQL.getContainerIpAddress(), postgreSQL.getMappedPort(5432)));
    // AMQP
    sysProps.put("amqp-host", amqpContainer.getContainerIpAddress());
    sysProps.put("amqp-port",
        String.valueOf(amqpContainer.getMappedPort(5671)));
    return sysProps;
  }

  @Override
  public void stop() {
    amqpContainer.stop();
    postgreSQL.stop();
  }
}
