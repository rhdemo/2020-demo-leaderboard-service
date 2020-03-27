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
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Game;
import com.redhat.developers.data.GameMessage;
import com.redhat.developers.data.Player;
import com.redhat.developers.sql.GameQueries;
import com.redhat.developers.sql.PlayerQueries;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;

/**
 * LeaderBoardStreamTest
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(QuarkusTestEnv.class)
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
  GameQueries gamesQ;

  @Inject
  GameInitService2 gameInitService;

  @Inject
  @Channel("leaderboard-persist-to-db")
  Emitter<String> playerEmitter;

  ReadOnlyKeyValueStore<String, Player> playersKVStore;

  @Inject
  Topology topology;



  @BeforeEach
  @SuppressWarnings("all")
  public void streamData() throws Exception {

    List<GameMessage> gameMessages = gameInitService.seedPlayers();

    KafkaProducer<String, String> kafkaProducer =
        new KafkaProducer<>(System.getProperties());

    assertNotNull(gameMessages);
    assertFalse(gameMessages.isEmpty());
    Multi.createFrom().iterable(gameMessages)
        .onItem().invoke(m -> {

          ProducerRecord<String, String> record =
              new ProducerRecord("demo2.my-topic", m.getPlayer().getPlayerId(),
                  jsonb.toJson(m));
          kafkaProducer.send(record);
        }).subscribe();
  }

  @Test()
  @Order(1)
  public void testPlayerPersistence() throws Exception {

    Optional<Game> gameExists = gameInitService.gameExist();
    assertTrue(gameExists.isPresent());

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
  }

  @Test()
  @Order(3)
  public void testPlayerPersistenceWithRowCount() throws Exception {
    assertNotNull(playersKVStore);

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

}
