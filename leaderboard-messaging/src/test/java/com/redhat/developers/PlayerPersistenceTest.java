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

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.sql.Connection;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Player;
import com.redhat.developers.sql.PlayerQueries;
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.consumer.KafkaConsumer;

/**
 * LeaderBoardStreamTest
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(AggreatorQuarkusTestEnv.class)
@TestInstance(Lifecycle.PER_CLASS)
public class PlayerPersistenceTest {

  static final Logger logger =
      Logger.getLogger(PlayerPersistenceTest.class.getName());

  @Inject
  Jsonb jsonb;

  @Inject
  Connection client;

  @Inject
  PlayerQueries playerQ;

  @Inject
  GameInitializer gameInitializer;

  Vertx vertx = Vertx.vertx();

  AtomicInteger testRecordCount = new AtomicInteger();

  @BeforeAll
  @SuppressWarnings("all")
  public void setupData() throws Exception {
    assertTrue(gameInitializer.gameExist().isPresent());
    Properties config = new Properties();
    config.put("bootstrap.servers", System.getProperty("bootstrap.servers"));
    config.put("group.id", getClass().getCanonicalName());
    config.put("key.deserializer",
        "org.apache.kafka.common.serialization.StringDeserializer");
    config.put("value.deserializer",
        "org.apache.kafka.common.serialization.StringDeserializer");
    config.put("auto.offset.reset", "earliest");
    config.put("enable.auto.commit", "false");

    KafkaConsumer<String, String> kafkaConsumer =
        KafkaConsumer.<String, String>create(vertx, config)
            .subscribe("my-topic");
    kafkaConsumer.handler(record -> {
      int i = testRecordCount.incrementAndGet();
      logger.log(Level.FINER, "Received Record {0} Key:{1} Value:{2} ",
          new Object[] {i, record.key(), record.value()});
    });
  }

  @Test
  @Order(1)
  public void testPlayerPersistence() throws Exception {

    nap();

    // Just to make sure the records are sent to Kafka
    Awaitility.await()
        .atMost(Duration.ofSeconds(10))
        .untilAtomic(this.testRecordCount,
            Is.is(Matchers.greaterThanOrEqualTo(3)));

    List<Player> players = playerQ
        .rankPlayers(client, 3);

    assertNotNull(players);
    assertEquals(3, players.size());
    Player aPlayer = players.get(0);
    assertNotNull(aPlayer);
    assertEquals(1, aPlayer.getPk());
    assertEquals("tom", aPlayer.getPlayerId());
    assertEquals("tom", aPlayer.getUsername());
    assertEquals(4, aPlayer.getRight());
    assertEquals(1, aPlayer.getWrong());
    assertEquals(100, aPlayer.getScore());

    aPlayer = players.get(1);
    assertNotNull(aPlayer);
    assertNotNull(aPlayer);
    assertEquals(3, aPlayer.getPk());
    assertEquals("jerry", aPlayer.getPlayerId());
    assertEquals("jerry", aPlayer.getUsername());
    assertEquals(4, aPlayer.getRight());
    assertEquals(1, aPlayer.getWrong());
    assertEquals(95, aPlayer.getScore());

    aPlayer = players.get(2);
    assertEquals(4, aPlayer.getPk());
    assertEquals("Winne", aPlayer.getPlayerId());
    assertEquals("Winne", aPlayer.getUsername());
    assertEquals(3, aPlayer.getRight());
    assertEquals(1, aPlayer.getWrong());
    assertEquals(95, aPlayer.getScore());

  }

  @Test
  @Order(2)
  public void testPlayerPersistenceWithRowCount() throws Exception {

    List<Player> players = playerQ
        .rankPlayers(client, 1);

    assertNotNull(players);
    assertEquals(1, players.size());

    Player aPlayer = players.get(0);
    assertNotNull(aPlayer);
    assertEquals(1, aPlayer.getPk());
    assertEquals("tom", aPlayer.getPlayerId());
    assertEquals("tom", aPlayer.getUsername());
    assertEquals(4, aPlayer.getRight());
    assertEquals(1, aPlayer.getWrong());
    assertEquals(100, aPlayer.getScore());

  }

  @AfterAll
  public void clearRecords() {
    gameInitializer.deleteGame();
    playerQ.delete(client, 1);
    playerQ.delete(client, 2);
    playerQ.delete(client, 3);
  }


  private void nap() throws Exception {
    Thread.sleep(2000);
  }
}
