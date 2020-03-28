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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.GameMessage;
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
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.mutiny.pgclient.PgPool;

/**
 * LeaderBoardStreamTest
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(QuarkusTestEnv.class)
@TestInstance(Lifecycle.PER_CLASS)
public class PlayerPersistenceTest {

  static final Logger logger =
      Logger.getLogger(PlayerPersistenceTest.class.getName());

  @Inject
  Jsonb jsonb;

  @Inject
  PgPool client;

  @Inject
  PlayerQueries playerQ;

  @Inject
  GameInitializer gameInitializer;

  List<GameMessage> gameMessages;

  AtomicInteger testRecordCount = new AtomicInteger();

  Vertx vertx = Vertx.vertx();
  KafkaProducer<String, String> kafkaProducer;

  @BeforeAll
  @SuppressWarnings("all")
  public void setupData() throws Exception {

    assertTrue(gameInitializer.gameExist().isPresent());

    kafkaProducer =
        KafkaProducer.create(vertx, System.getProperties());
    assertNotNull(kafkaProducer);

    URL dataFileUrl = this.getClass().getResource("/data.json");
    gameMessages = jsonb.fromJson(dataFileUrl.openStream(),
        new ArrayList<GameMessage>() {}.getClass()
            .getGenericSuperclass());

    assertNotNull(gameMessages);
    assertFalse(gameMessages.isEmpty());

    gameMessages.forEach(m -> {
      KafkaProducerRecord gameRecord =
          KafkaProducerRecord.create("my-topic", m.getPlayer().getPlayerId(),
              jsonb.toJson(m));
      kafkaProducer.send(gameRecord, rm -> {
        if (rm.succeeded()) {
          int i = testRecordCount.incrementAndGet();
          logger.log(Level.FINE, "Inserted {0} records to {1} at {2}",
              new Object[] {i - 1, rm.result().getTopic(),
                  rm.result().getTimestamp()});
        } else {
          fail(rm.cause());
        }
      });
    });
  }

  @Test
  @Order(1)
  @SuppressWarnings("all")
  public void testPlayerPersistence() throws Exception {

    Awaitility.await().untilAtomic(testRecordCount,
        Is.is(Matchers.greaterThan(3)));

    kafkaProducer.flush(x -> {

      Optional<List<Player>> optPlayers = playerQ
          .rankPlayers(client, 3)
          .await().asOptional().atMost(Duration.ofSeconds(10));

      assertTrue(optPlayers.isPresent());

      List<Player> players = optPlayers.get();
      assertNotNull(players);
      assertEquals(3, players.size());
      Player aPlayer = players.get(0);
      assertNotNull(aPlayer);
      assertEquals("tom", aPlayer.getId());
      assertEquals("tom", aPlayer.getUsername());
      assertEquals(7, aPlayer.getRight());
      assertEquals(2, aPlayer.getWrong());
      assertEquals(100, aPlayer.getScore());

      aPlayer = players.get(1);
      assertNotNull(aPlayer);
      assertNotNull(aPlayer);
      assertEquals("jerry", aPlayer.getId());
      assertEquals("jerry", aPlayer.getUsername());
      assertEquals(4, aPlayer.getRight());
      assertEquals(1, aPlayer.getWrong());
      assertEquals(95, aPlayer.getScore());

      aPlayer = players.get(2);
      assertEquals("Winne", aPlayer.getId());
      assertEquals("Winne", aPlayer.getUsername());
      assertEquals(3, aPlayer.getRight());
      assertEquals(1, aPlayer.getWrong());
      assertEquals(95, aPlayer.getScore());

    });
  }

  // @Test
  // @Order(3)
  public void testPlayerPersistenceWithRowCount() throws Exception {

    Optional<List<Player>> optPlayers = playerQ
        .rankPlayers(client, 3)
        .await().asOptional().atMost(Duration.ofSeconds(10));

    assertTrue(optPlayers.isPresent());

    List<Player> players = optPlayers.get();

    assertNotNull(players);
    assertEquals(1, players.size());

    Player aPlayer = players.get(0);
    assertNotNull(aPlayer);
    assertEquals("tom", aPlayer.getId());
    assertEquals("tom", aPlayer.getUsername());
    assertEquals(7, aPlayer.getRight());
    assertEquals(2, aPlayer.getWrong());
    assertEquals(100, aPlayer.getScore());

  }

  @AfterAll
  public void clearRecords() {
    gameInitializer.deleteGame();
    playerQ.delete(client, 1);
    playerQ.delete(client, 2);
    playerQ.delete(client, 3);
  }

}
