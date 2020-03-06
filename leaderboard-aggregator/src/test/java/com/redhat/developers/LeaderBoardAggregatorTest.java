package com.redhat.developers;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

import static com.redhat.developers.QuarkusTestEnv.*;

/**
 * LeaderBoardRankingsTest
 */
@QuarkusTest
@QuarkusTestResource(QuarkusTestEnv.class)
public class LeaderBoardAggregatorTest {


  @BeforeEach
  public void checkEnv() {
    String bootstrapServer = kafka.getContainerIpAddress() + ":"
        + kafka.getMappedPort(kafka.getKafkaPort());
    assertEquals(bootstrapServer,
        System.getProperty("kafka.bootstrap.servers"));
    String postgeSql = String.format(JDBC_URL,
        postgreSQL.getContainerIpAddress(), postgreSQL.getMappedPort(5432));

    assertEquals(bootstrapServer,
        System.getProperty("kafka.bootstrap.servers"));
    assertEquals(bootstrapServer,
        System.getProperty("quarkus.kafka-streams.bootstrap-servers"));
    assertEquals(postgeSql,
        System.getProperty("quarkus.datasource.url"));
  }

  @Test
  public void testKeyStore() {
    fail("not implemented");
  }
}
