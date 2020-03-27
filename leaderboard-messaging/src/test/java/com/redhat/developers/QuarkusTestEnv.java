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
import java.util.logging.Level;
import java.util.logging.Logger;
import com.redhat.developers.containers.PostgreSqlContainer;
import com.redhat.developers.containers.QdrouterContainer;
import com.redhat.developers.data.Avatar;
import com.redhat.developers.data.GameMessage;
import com.redhat.developers.data.Player;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Testing;
import io.quarkus.kafka.client.serialization.JsonbSerde;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * QuarkusTestEnv
 */
public class QuarkusTestEnv implements QuarkusTestResourceLifecycleManager {

  Logger logger = Logger.getLogger(QuarkusTestEnv.class.getName());

  public final static String JDBC_URL =
      "vertx-reactive:postgresql://%s:%d/gamedb";

  final static Network net = Network.newNetwork();


  @Container
  public static PostgreSqlContainer postgreSQL = new PostgreSqlContainer();

  @Container
  public static QdrouterContainer amqpContainer = new QdrouterContainer();

  Topology topology;

  @Override
  public Map<String, String> start() {

    amqpContainer.start();
    postgreSQL.start();
    Map<String, String> sysProps = new HashMap<>();
    // Quarkus
    sysProps.put("quarkus.http.test-port", "8085");
    // Kafka and Kafka Streams
    sysProps.put("quarkus.kafka-streams.application-id", "demo2.my-topic");
    sysProps.put("rhdemo.leaderboard.kvstore.name", "messaging-test");
    sysProps.put("rhdemo.leaderboard.aggregator.stream", "transactions");
    sysProps.put("quarkus.kafka-streams.topics", "demo2.my-topic");
    sysProps.put("kafka-streams.default.key.serde",
        "org.apache.kafka.common.serialization.Serdes$StringSerde");
    sysProps.put("kafka-streams.default.value.serde",
        "org.apache.kafka.common.serialization.Serdes$StringSerde");
    sysProps.put("kafka-streams.default.deserialization.exception.handler",
        "org.apache.kafka.streams.errors.LogAndContinueExceptionHandler");
    sysProps.put("quarkus.kafka-streams.bootstrap-servers", "localhost:9092");
    sysProps.put("kafka.bootstrap.servers", "localhost:9092");
    sysProps.put("kafka-streams.cache.max.bytes.buffering", "10240");
    sysProps.put("kafka-streams.commit.interval.ms", "500");
    sysProps.put("kafka-streams.metadata.max.age.ms", "500");
    sysProps.put("kafka-streams.auto.offset.reset", "earliest");
    sysProps.put("acks", "all");
    sysProps.put("bootstrap.servers", "localhost:9092");
    sysProps.put("key.serializer",
        "org.apache.kafka.common.serialization.StringSerializer");
    sysProps.put("value.serializer",
        "org.apache.kafka.common.serialization.StringSerializer");

    // Database
    // quarkus.datasource.url=vertx-reactive:postgresql://localhost:5432/gamedb
    sysProps.put("quarkus.datasource.url", String.format(JDBC_URL,
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
      amqpContainer.stop();
      postgreSQL.stop();
    } catch (Exception e) {

    }
  }
}
