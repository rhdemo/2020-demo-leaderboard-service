package com.redhat.developers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import com.redhat.developers.containers.PostgreSqlContainer;
import com.redhat.developers.containers.StrimziKafkaContainer;
import com.redhat.developers.containers.ZookeeperContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * QuarkusTestEnv
 */
public class QuarkusTestEnv implements QuarkusTestResourceLifecycleManager {

  Logger logger = Logger.getLogger(QuarkusTestEnv.class.getName());

  public final static String JDBC_URL =
      "vertx-reactive:postgresql://%s:%d/gamedb";

  private static Network network = Network.newNetwork();

  @Container
  public static ZookeeperContainer zookeeper =
      new ZookeeperContainer(network).waitingFor(Wait.forListeningPort());

  @Container
  public static StrimziKafkaContainer kafka =
      new StrimziKafkaContainer(network, zookeeper)
          .waitingFor(Wait.forListeningPort());


  @Container
  public static PostgreSqlContainer postgreSQL = new PostgreSqlContainer();

  @Override
  public Map<String, String> start() {
    logger.info("Starting Kafka");
    zookeeper.start();
    kafka.start();
    postgreSQL.start();
    Map<String, String> sysProps = new HashMap<>();
    String bootstrapServer = kafka.getContainerIpAddress() + ":"
        + kafka.getMappedPort(kafka.getKafkaPort());
    sysProps.put("kafka.bootstrap.servers", bootstrapServer);
    sysProps.put("quarkus.kafka-streams.bootstrap-servers", bootstrapServer);
    // quarkus.datasource.url=vertx-reactive:postgresql://localhost:5432/gamedb
    sysProps.put("quarkus.datasource.url", String.format(JDBC_URL,
        postgreSQL.getContainerIpAddress(), postgreSQL.getMappedPort(5432)));
    return sysProps;
  }

  @Override
  public void stop() {
    kafka.stop();
    zookeeper.stop();
    postgreSQL.stop();
  }
}
