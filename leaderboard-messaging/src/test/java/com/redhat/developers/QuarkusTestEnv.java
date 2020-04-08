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
import com.redhat.developers.containers.QdrouterContainer;
import com.redhat.developers.containers.StrimziKafkaContainer;
import com.redhat.developers.containers.StrimziZookeeperContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * QuarkusTestEnv
 */
@SuppressWarnings("all")
public class QuarkusTestEnv
    implements QuarkusTestResourceLifecycleManager {

  Logger logger = Logger.getLogger(QuarkusTestEnv.class.getName());

  public final static String JDBC_URL =
      "jdbc:postgresql://%s:%d/gamedb";

  public static final PostgreSqlContainer postgreSQL =
      new PostgreSqlContainer();

  public static StrimziZookeeperContainer zookeeper =
      new StrimziZookeeperContainer()
          .waitingFor(Wait.forListeningPort());

  public static final StrimziKafkaContainer kafka =
      new StrimziKafkaContainer()
          .dependsOn(zookeeper)
          .waitingFor(Wait.forListeningPort());

  public static QdrouterContainer amqpContainer = new QdrouterContainer();

  @Override
  public Map<String, String> start() {

    try {
      kafka.start();
      postgreSQL.start();
      amqpContainer.start();
    } catch (Exception e) {
      e.printStackTrace();
    }


    while (!kafka.isRunning()) {
      try {
        logger.info("Waiting for Kafka to be started..");
        Thread.sleep(3000);
      } catch (InterruptedException e) {
      }
    }

    Map<String, String> sysProps = new HashMap<>();

    // Quarkus
    sysProps.put("quarkus.http.test-port", "8085"); // Kafka
    sysProps.put("quarkus.kafka-streams.bootstrap-servers",
        kafka.getBootstrapServers());
    sysProps.put("quarkus.datasource.username", "demo");
    sysProps.put("quarkus.datasource.password", "password!");
    sysProps.put("quarkus.datasource.jdbc.url", String.format(JDBC_URL,
        postgreSQL.getContainerIpAddress(), postgreSQL.getMappedPort(5432)));

    // AMQP
    sysProps.put("amqp-host", amqpContainer.getContainerIpAddress());
    sysProps.put("amqp-port",
        String.valueOf(amqpContainer.getMappedPort(5671)));
    sysProps.put("skupper.messaging.ca.cert.path",
        "src/test/resources/ssl/ca.crt");
    sysProps.put("skupper.messaging.cert.path",
        "src/test/resources/ssl/tls.crt");
    sysProps.put("skupper.messaging.key.path",
        "src/test/resources/ssl/tls.key");

    return sysProps;
  }

  @Override
  public void stop() {
    try {
      kafka.stop();
      postgreSQL.stop();
      amqpContainer.stop();
    } catch (Exception e) {

    }
  }
}
