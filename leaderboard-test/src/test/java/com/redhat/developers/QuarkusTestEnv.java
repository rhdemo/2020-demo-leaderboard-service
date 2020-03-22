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
    sysProps.put("quarkus.kafka-streams.topics", "dummy-topic");
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
