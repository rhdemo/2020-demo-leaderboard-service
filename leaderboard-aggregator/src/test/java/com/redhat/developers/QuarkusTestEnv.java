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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import com.redhat.developers.containers.PostgreSqlContainer;
import com.redhat.developers.containers.StrimziKafkaContainer;
import com.redhat.developers.containers.StrimziZookeeperContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * QuarkusTestEnv
 */
@SuppressWarnings("all")
public class QuarkusTestEnv implements QuarkusTestResourceLifecycleManager {

  Logger logger = Logger.getLogger(QuarkusTestEnv.class.getName());

  public final static String JDBC_URL =
      "vertx-reactive:postgresql://%s:%d/gamedb";

  public static final PostgreSqlContainer postgreSQL =
      new PostgreSqlContainer();

  public static StrimziZookeeperContainer zookeeper =
      new StrimziZookeeperContainer()
          .waitingFor(Wait.forListeningPort());

  public static final StrimziKafkaContainer kafka =
      new StrimziKafkaContainer()
          .dependsOn(zookeeper)
          .waitingFor(Wait.forListeningPort());

  @Override
  public Map<String, String> start() {
    kafka.start();
    postgreSQL.start();
    Map<String, String> sysProps = new HashMap<>();
    // Quarkus
    sysProps.put("quarkus.http.test-port", "8085");
    // Kafka and Kafka Streams
    sysProps.put("bootstrap.servers", kafka.getBootstrapServers());
    sysProps.put("acks", "1");
    sysProps.put("kafka.bootstrap.servers", kafka.getBootstrapServers());
    sysProps.put(
        "mp.messaging.incoming.leaderboard-persist-to-db.bootstrap.servers",
        kafka.getBootstrapServers());
    sysProps.put("key.serializer",
        "org.apache.kafka.common.serialization.StringSerializer");
    sysProps.put("value.serializer",
        "org.apache.kafka.common.serialization.StringSerializer");

    // Database
    // quarkus.datasource.url=vertx-reactive:postgresql://localhost:5432/gamedb
    sysProps.put("quarkus.datasource.url", String.format(JDBC_URL,
        postgreSQL.getContainerIpAddress(), postgreSQL.getMappedPort(5432)));

    return sysProps;
  }

  @Override
  public void stop() {
    try {
      kafka.stop();
      postgreSQL.stop();
    } catch (Exception e) {

    }
  }
}
