/*-
 * #%L
 * Leaderboard SQL
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import com.redhat.developers.data.Game;
import com.redhat.developers.data.GameState;
import com.redhat.developers.sql.GameQueries;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.mutiny.pgclient.PgPool;

/**
 * GameQueriesTest
 */
@QuarkusTest
@QuarkusTestResource(QuarkusTestEnv.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameQueriesTest {

  @Inject
  GameQueries gameQueries;

  @Inject
  PgPool client;

  @Order(1)
  @Test
  public void testAdd() throws Exception {
    Game game = Game.newGame()
        .gameId("saveTest001")
        .state(GameState.byCode(1))
        .configuration("{}");

    Optional<Boolean> isUpserted = gameQueries
        .upsert(client, game)
        .await().asOptional().atMost(Duration.ofSeconds(10));

    assertTrue(isUpserted.isPresent());
    assertTrue(isUpserted.get());
  }

  @Order(2)
  @Test
  public void testFindAll() throws Exception {
    Game game = Game.newGame()
        .id(1)
        .gameId("saveTest001")
        .state(GameState.byCode(1))
        .configuration("{}");

    Optional<List<Game>> og = gameQueries.findAll(client)
        .await().asOptional().atMost(Duration.ofSeconds(10));
    assertTrue(og.isPresent());
    List<Game> games = og.get();
    assertNotNull(games);
    assertTrue(games.size() == 1);
    Game actualGame = games.get(0);
    assertNotNull(actualGame);
    assertEquals(game.getId(), actualGame.getId());
    assertEquals(game.getState(), actualGame.getState());
    assertEquals(game.getGameId(), actualGame.getGameId());
    assertEquals(game.getConfiguration(), actualGame.getConfiguration());
    assertNotNull(actualGame.getDate());
  }

  @Order(3)
  @Test
  public void testFindById() throws Exception {
    Game game = Game.newGame()
        .id(1)
        .gameId("saveTest001")
        .state(GameState.byCode(1))
        .configuration("{}");

    Optional<Optional<Game>> og = gameQueries.findById(client, 1)
        .await().asOptional().atMost(Duration.ofSeconds(10));
    assertTrue(og.isPresent());
    Optional<Game> optGame = og.get();
    assertTrue(optGame.isPresent());
    Game actualGame = optGame.get();
    assertNotNull(actualGame);
    assertEquals(game.getId(), actualGame.getId());
    assertEquals(game.getState(), actualGame.getState());
    assertEquals(game.getGameId(), actualGame.getGameId());
    assertEquals(game.getConfiguration(), actualGame.getConfiguration());
    assertNotNull(actualGame.getDate());
  }

  @Order(4)
  @Test
  public void testUpsert() throws Exception {
    Game game = Game.newGame()
        .id(1)
        .gameId("saveTest001")
        .state(GameState.byCode(2))
        .configuration("{}");

    Optional<Boolean> isUpserted = gameQueries
        .upsert(client, game)
        .await().asOptional().atMost(Duration.ofSeconds(10));

    assertTrue(isUpserted.isPresent());
    assertTrue(isUpserted.get());

    // Query
    Optional<Optional<Game>> og = gameQueries.findById(client, 1)
        .await().asOptional().atMost(Duration.ofSeconds(10));
    assertTrue(og.isPresent());
    Optional<Game> optGame = og.get();
    assertTrue(optGame.isPresent());
    Game actualGame = optGame.get();
    assertNotNull(actualGame);
    assertEquals(1, actualGame.getId());
    assertEquals(GameState.byCode(2), actualGame.getState());
    assertEquals("saveTest001", actualGame.getGameId());
  }

  @Order(5)
  @Test
  public void testDelete() throws Exception {
    Optional<Boolean> og = gameQueries.delete(client, 1)
        .await().asOptional().atMost(Duration.ofSeconds(10));
    assertTrue(og.isPresent());
    assertTrue(og.get());
  }

  @Order(6)
  @Test
  public void testNotFound() throws Exception {
    Optional<Optional<Game>> og = gameQueries.findById(client, 1)
        .await().asOptional().atMost(Duration.ofSeconds(10));
    assertFalse(og.get().isPresent());
  }
}
