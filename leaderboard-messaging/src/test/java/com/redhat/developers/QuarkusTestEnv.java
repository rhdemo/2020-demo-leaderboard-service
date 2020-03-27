/*-
 * #%L
 * Leaderboard Aggregator Test
 * %%
 * Copyright (C) 2020 Red Hat Inc.,
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.redhat.developers;

import static org.junit.jupiter.api.Assertions.fail;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import com.redhat.developers.containers.PostgreSqlContainer;
import com.redhat.developers.containers.QdrouterContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Testing;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * QuarkusTestEnv
 */
public class QuarkusTestEnv implements QuarkusTestResourceLifecycleManager {

  Logger logger = Logger.getLogger(QuarkusTestEnv.class.getName());

  public final static String JDBC_URL =
      "vertx-reactive:postgresql://%s:%d/gamedb";

  final static Network net = Network.newNetwork();

  KafkaCluster kafkaCluster;

  @Container
  public static PostgreSqlContainer postgreSQL = new PostgreSqlContainer();

  @Container
  public static QdrouterContainer amqpContainer = new QdrouterContainer();

  @Override
  public Map<String, String> start() {
    // try {
    // startKafkaCluster();
    // } catch (IOException e) {
    // fail("Unable to start Kafka", e);
    // }
    amqpContainer.start();
    postgreSQL.start();
    Map<String, String> sysProps = new HashMap<>();
    sysProps.put("quarkus.kafka-streams.topics", "demo2.my-topic");
    sysProps.put("quarkus.http.test-port", "8085");
    // quarkus.datasource.url=vertx-reactive:postgresql://localhost:5432/gamedb
    sysProps.put("quarkus.datasource.url", String.format(JDBC_URL,
        postgreSQL.getContainerIpAddress(), postgreSQL.getMappedPort(5432)));
    // AMQP
    sysProps.put("amqp-host", amqpContainer.getContainerIpAddress());
    sysProps.put("amqp-port",
        String.valueOf(amqpContainer.getMappedPort(5671)));
    sysProps.put("acks", "all");
    sysProps.put("bootstrap.servers", "localhost:9092");
    sysProps.put("key.serializer",
        "org.apache.kafka.common.serialization.StringSerializer");
    sysProps.put("value.serializer",
        "org.apache.kafka.common.serialization.StringSerializer");
    return sysProps;
  }

  private void startKafkaCluster() throws IOException {
    Properties props = new Properties();
    props.setProperty("zookeeper.connection.timeout.ms", "10000");
    File directory = Testing.Files
        .createTestingDirectory(System.getProperty("java.io.tmpdir"), true);
    kafkaCluster = new KafkaCluster().withPorts(2182, 9092).addBrokers(1)
        .usingDirectory(directory)
        .deleteDataUponShutdown(true)
        .withKafkaConfiguration(props)
        .deleteDataPriorToStartup(true)
        .startup();
    kafkaCluster.createTopic("demo2.my-topic", 1, 1);
  }

  @Override
  public void stop() {
    try {
      kafkaCluster.shutdown();
      amqpContainer.stop();
      postgreSQL.stop();
    } catch (Exception e) {

    }
  }
}
