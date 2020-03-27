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

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Game;
import com.redhat.developers.data.GameMessage;
import com.redhat.developers.data.Player;
import com.redhat.developers.sql.GameQueries;
import com.redhat.developers.sql.PlayerQueries;
import com.redhat.developers.util.Scorer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import io.quarkus.kafka.client.serialization.JsonbSerde;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.vertx.axle.pgclient.PgPool;

/**
 * LeaderBoardStreamTest
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(QuarkusTestEnv.class)
public class LeaderBoardTest {

  static final Logger logger =
      Logger.getLogger(LeaderBoardTest.class.getName());


  private static List<GameMessage> scoringMessages = new ArrayList<>();

  @ConfigProperty(name = "rhdemo.leaderboard.kvstore.name")
  String kvStoreName;

  @ConfigProperty(name = "rhdemo.leaderboard.aggregator.stream")
  String outStream;

  JsonbSerde<GameMessage> gameMessageSerde =
      new JsonbSerde<>(GameMessage.class);
  JsonbSerde<Player> playerSerde = new JsonbSerde<>(Player.class);

  @Inject
  Jsonb jsonb;

  @Inject
  Topology topology;

  @Inject
  PgPool client;

  @Inject
  KafkaStreams kafkaStreams;

  @Inject
  PlayerQueries playerQ;

  @Inject
  GameQueries gamesQ;

  @Inject
  @Channel("leaderboard-persist-to-db")
  Emitter<String> playerEmitter;

  ReadOnlyKeyValueStore<String, Player> playersKVStore;

  @BeforeAll
  public static void setupData() {
    logger.info("Setting up test data");
    try {
      scoringMessages =
          Scorer.fromFile(
              LeaderBoardTest.class.getResourceAsStream("/data.json"));
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error loading test data", e);
    }
  }


  @BeforeEach
  public void streamData() {
    assertNotNull(topology);
    assertNotNull(kafkaStreams);

    KafkaProducer<String, String> kafkaProducer =
        new KafkaProducer<>(System.getProperties());

    assertNotNull(scoringMessages);
    assertFalse(scoringMessages.isEmpty());
    scoringMessages.stream()
        .forEach(m -> {
          ProducerRecord<String, String> record =
              new ProducerRecord<>("demo2.my-topic", m.getPlayer().id,
                  jsonb.toJson(m));
          kafkaProducer.send(record);
        });

    while (true) {
      // Verify the records from store, Wait for the store to be
      try {
        playersKVStore =
            kafkaStreams.store(kvStoreName,
                QueryableStoreTypes.keyValueStore());
        if (playersKVStore != null) {
          break;
        }
      } catch (Exception e) {
      }
    }

  }

  @Test
  @Order(1)
  public void testPlayerScoreAggregation() {

    assertNotNull(playersKVStore);
    Player aPlayer = playersKVStore.get("new-game-1583157438~tom");
    assertNotNull(aPlayer);
    assertEquals("tom", aPlayer.getId());
    assertEquals("tom", aPlayer.getUsername());
    assertEquals(7, aPlayer.getRight());
    assertEquals(2, aPlayer.getWrong());
    assertEquals(195, aPlayer.getScore());

    aPlayer = playersKVStore.get("new-game-1583157438~Winne");
    assertNotNull(aPlayer);
    assertEquals("Winne", aPlayer.getId());
    assertEquals("Winne", aPlayer.getUsername());
    assertEquals(3, aPlayer.getRight());
    assertEquals(1, aPlayer.getWrong());
    assertEquals(95, aPlayer.getScore());

    aPlayer = playersKVStore.get("new-game-1583157438~jerry");
    assertNotNull(aPlayer);
    assertEquals("jerry", aPlayer.getId());
    assertEquals("jerry", aPlayer.getUsername());
    assertEquals(4, aPlayer.getRight());
    assertEquals(1, aPlayer.getWrong());
    assertEquals(95, aPlayer.getScore());
  }

  @Test()
  @Order(2)
  public void testPlayerPersistence() {
    assertNotNull(playersKVStore);


    // PST
    OffsetDateTime somePSTDateTime = OffsetDateTime.of(
        LocalDateTime.of(2020, Month.MARCH, 9, 18, 01, 00),
        ZoneOffset.ofHoursMinutes(-7, 0));

    // Some GMT time
    OffsetDateTime someGMTDateTime = OffsetDateTime.of(
        LocalDateTime.of(2020, Month.MARCH, 9, 18, 01, 00),
        ZoneOffset.ofHoursMinutes(0, 0));

    Game game = Game.newGame()
        .gameId("new-game-1583157436")
        .state("stopped")
        .date(somePSTDateTime);
    gamesQ.upsert(client, game);

    game = Game.newGame()
        .gameId("new-game-1583157437")
        .state("paused")
        .date(someGMTDateTime);
    gamesQ.upsert(client, game);

    game = Game.newGame()
        .gameId("new-game-1583157438")
        .state("active")
        .date(OffsetDateTime.now());

    gamesQ.upsert(client, game);
    // Test Persistence
    this.playersKVStore.all()
        .forEachRemaining(a -> playerEmitter.send(jsonb.toJson(a.value)));

    // Sleep for 1/2 seconds - just to give some time db to be updated with all three records
    try {
      Thread.sleep(500);
    } catch (Exception e) {

    }

    try {

      List<Player> players = playerQ.rankPlayers(client, 10)
          .toCompletableFuture().join();
      assertNotNull(players);
      assertEquals(3, players.size());
      Player aPlayer = players.get(0);
      assertNotNull(aPlayer);
      assertEquals("tom", aPlayer.getId());
      assertEquals("tom", aPlayer.getUsername());
      assertEquals(7, aPlayer.getRight());
      assertEquals(2, aPlayer.getWrong());
      assertEquals(195, aPlayer.getScore());

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


    } catch (CompletionException e) {
      fail(e);
    }
  }

  @Test()
  @Order(3)
  public void testPlayerPersistenceWithRowCount() {
    assertNotNull(playersKVStore);


    // PST
    OffsetDateTime somePSTDateTime = OffsetDateTime.of(
        LocalDateTime.of(2020, Month.MARCH, 9, 18, 01, 00),
        ZoneOffset.ofHoursMinutes(-7, 0));

    // Some GMT time
    OffsetDateTime someGMTDateTime = OffsetDateTime.of(
        LocalDateTime.of(2020, Month.MARCH, 9, 18, 01, 00),
        ZoneOffset.ofHoursMinutes(0, 0));

    Game game = Game.newGame()
        .gameId("new-game-1583157436")
        .state("stopped")
        .date(somePSTDateTime);
    gamesQ.upsert(client, game);

    game = Game.newGame()
        .gameId("new-game-1583157437")
        .state("paused")
        .date(someGMTDateTime);
    gamesQ.upsert(client, game);

    game = Game.newGame()
        .gameId("new-game-1583157438")
        .state("active")
        .date(OffsetDateTime.now());

    gamesQ.upsert(client, game);
    // Test Persistence
    this.playersKVStore.all()
        .forEachRemaining(a -> playerEmitter.send(jsonb.toJson(a.value)));

    // Sleep for 1/2 seconds - just to give some time db to be updated with all three records
    try {
      Thread.sleep(500);
    } catch (Exception e) {

    }

    try {

      Response response = given()
          .when()
          .get("/api/leaderboard?rowCount=1")
          .then()
          .contentType(ContentType.JSON)
          .extract().response();

      assertEquals(200, response.statusCode());

      String jsonStr = response.getBody().asString();

      List<Player> players = jsonb.fromJson(jsonStr,
          new ArrayList<Player>() {}.getClass().getGenericSuperclass());

      assertNotNull(players);
      assertEquals(1, players.size());

      Player aPlayer = players.get(0);
      assertNotNull(aPlayer);
      assertEquals("tom", aPlayer.getId());
      assertEquals("tom", aPlayer.getUsername());
      assertEquals(7, aPlayer.getRight());
      assertEquals(2, aPlayer.getWrong());
      assertEquals(195, aPlayer.getScore());

    } catch (CompletionException e) {
      fail(e);
    }
  }

}
