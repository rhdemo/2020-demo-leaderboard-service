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
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Avatar;
import com.redhat.developers.data.Game;
import com.redhat.developers.data.Player;
import com.redhat.developers.sql.PlayerQueries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.mutiny.pgclient.PgPool;

/**
 * PlayerQueriesTest
 */
@QuarkusTest
@QuarkusTestResource(QuarkusTestEnv.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PlayerQueriesTest {

  @Inject
  GameInitService gameInitService;

  @Inject
  PlayerQueries playerQueries;

  @Inject
  PgPool client;

  @Inject
  Jsonb jsonb;

  @BeforeEach
  public void checkIfGameExist() {
    Optional<Game> thisGame = gameInitService.gameExist();
    assertTrue(thisGame.isPresent());
  }

  @Order(1)
  @Test
  public void testAdd() throws Exception {
    Game game = gameInitService.game;
    Player player = Player.newPlayer()
        .avatar(avatar())
        .playerId("tom")
        .username("Tom and Jerry")
        .score(10)
        .right(5)
        .wrong(2)
        .gameId(game.getGameId())
        .creationServer("BLR")
        .gameServer("BLR")
        .scoringServer("BLR");

    Optional<Boolean> isUpserted = playerQueries
        .upsert(client, player)
        .await().asOptional().atMost(Duration.ofSeconds(10));

    assertTrue(isUpserted.isPresent());
    assertTrue(isUpserted.get());
  }

  @Order(2)
  @Test
  public void testFindAll() throws Exception {
    Game game = gameInitService.game;
    Player player = Player.newPlayer()
        .avatar(avatar())
        .playerId("tom")
        .username("Tom and Jerry")
        .score(10)
        .right(5)
        .wrong(2)
        .gameId(game.getGameId())
        .creationServer("BLR")
        .gameServer("BLR")
        .scoringServer("BLR");

    Optional<Optional<Player>> isUpserted = playerQueries
        .findById(client, 1)
        .await().asOptional().atMost(Duration.ofSeconds(10));

    assertTrue(isUpserted.isPresent());
    Optional<Player> optPlayer = isUpserted.get();
    assertTrue(optPlayer.isPresent());
    Player actualPlayer = optPlayer.get();
    assertPlayer(1, player, actualPlayer);
    Avatar actualAvatar = actualPlayer.getAvatar();
    assertAvatar(actualAvatar);
  }

  @Order(3)
  @Test
  public void testUpsert() throws Exception {
    Game game = gameInitService.game;
    Player player = Player.newPlayer()
        .id(1)
        .avatar(avatar())
        .playerId("tom")
        .username("Tom and Jerry")
        .score(20)
        .right(7)
        .wrong(2)
        .gameId(game.getGameId())
        .creationServer("BLR")
        .gameServer("MAA")
        .scoringServer("BLR");

    Optional<Boolean> isUpserted = playerQueries
        .upsert(client, player)
        .await().asOptional().atMost(Duration.ofSeconds(10));

    assertTrue(isUpserted.isPresent());
    assertTrue(isUpserted.get());

    // Check by Querying back
    Optional<Optional<Player>> upserted = playerQueries
        .findById(client, 1)
        .await().asOptional().atMost(Duration.ofSeconds(10));
    assertTrue(isUpserted.isPresent());

    Optional<Player> optPlayer = upserted.get();
    assertTrue(optPlayer.isPresent());
    Player actualPlayer = optPlayer.get();
    assertPlayer(1, player, actualPlayer);
    Avatar actualAvatar = actualPlayer.getAvatar();
    assertAvatar(actualAvatar);
  }

  @Order(4)
  @Test
  public void testRankPlayers() throws Exception {
    List<Player> seededPlayers = seedPlayers();
    Optional<List<Player>> optRankedPlayers = playerQueries
        .rankPlayers(client, 3)
        .await().asOptional().atMost(Duration.ofSeconds(10));

    assertTrue(optRankedPlayers.isPresent());
    List<Player> players = optRankedPlayers.get();
    assertEquals(3, players.size());
    Player firstPlayer = players.get(0);
    // Check if tom wins the game
    assertEquals("tom", firstPlayer.getPlayerId());
    // Check all the other parameters
    assertPlayer(1, seededPlayers.get(0), players.get(0));
    assertPlayer(4, seededPlayers.get(1), players.get(1));
    assertPlayer(5, seededPlayers.get(2), players.get(2));
  }

  @Order(5)
  @Test
  public void testDelete() throws Exception {
    Optional<Boolean> isDeleted = playerQueries
        .delete(client, 1)
        .await().asOptional().atMost(Duration.ofSeconds(10));

    assertTrue(isDeleted.isPresent());
    assertTrue(isDeleted.get());
  }

  @Order(6)
  @Test
  public void testNoPlayer() throws Exception {
    Optional<Optional<Player>> player = playerQueries
        .findById(client, 1)
        .await().asOptional().atMost(Duration.ofSeconds(10));

    assertTrue(player.isPresent());
    assertFalse(player.get().isPresent());
  }

  private void assertPlayer(int id, Player player, Player actualPlayer) {
    assertNotNull(actualPlayer);
    assertEquals(id, actualPlayer.getId(), "id mismatch");
    assertEquals(player.getPlayerId(), actualPlayer.getPlayerId(),
        "Player Id mismatch");
    assertEquals(player.getGameId(), actualPlayer.getGameId(),
        "Player Game mismatch");
    assertEquals(player.getUsername(), actualPlayer.getUsername(),
        "Player Username mismatch");
    assertEquals(player.getCreationServer(), actualPlayer.getCreationServer(),
        "Player Creation Server mismatch");
    assertEquals(player.getScoringServer(), actualPlayer.getScoringServer(),
        "Player Scoring Server mismatch");
    assertEquals(player.getGameServer(), actualPlayer.getGameServer(),
        "Player Game Server mismatch");
    assertEquals(player.getScore(), actualPlayer.getScore(),
        "Player score mismatch");
    assertEquals(player.getRight(), actualPlayer.getRight(),
        "Player guess rights mismatch");
    assertEquals(player.getWrong(), actualPlayer.getWrong(),
        "Player guess wrong mismatch");
  }

  private void assertAvatar(Avatar actualAvatar) {
    assertNotNull(actualAvatar);
    assertEquals(avatar().body, actualAvatar.body);
    assertEquals(avatar().color, actualAvatar.color);
    assertEquals(avatar().eyes, actualAvatar.eyes);
    assertEquals(avatar().ears, actualAvatar.ears);
    assertEquals(avatar().nose, actualAvatar.nose);
    assertEquals(avatar().mouth, actualAvatar.mouth);
  }

  private Avatar avatar() {
    return Avatar.newAvatar()
        .body(1)
        .color(2)
        .ears(3)
        .eyes(4)
        .nose(5)
        .mouth(6);
  }

  private List<Player> seedPlayers() throws Exception {
    List<Player> players = gameInitService.seedPlayers();
    players.stream()
        .forEach(p -> {
          Optional<Boolean> isUpserted = playerQueries
              .upsert(client, p)
              .await().asOptional().atMost(Duration.ofSeconds(10));
          assertTrue(isUpserted.get());
        });
    return players;
  }

}
