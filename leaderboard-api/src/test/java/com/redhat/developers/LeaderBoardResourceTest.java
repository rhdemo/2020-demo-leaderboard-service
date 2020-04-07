/*-
 * #%L
 * Leaderboard API
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Game;
import com.redhat.developers.data.GameMessage;
import com.redhat.developers.data.GameState;
import com.redhat.developers.data.Player;
import com.redhat.developers.model.Leaderboard;
import com.redhat.developers.sql.GameQueries;
import com.redhat.developers.sql.PlayerQueries;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;

/**
 * LeaderBoardResourceTest
 */
@QuarkusTest
@QuarkusTestResource(APIQuarkusTestEnv.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LeaderBoardResourceTest {

  static final Logger logger =
      Logger.getLogger(LeaderBoardResourceTest.class.getName());

  @Inject
  Jsonb jsonb;

  @Inject
  @Named("gamedb")
  Connection client;

  @Inject
  PlayerQueries playerQueries;

  @Inject
  GameQueries gameQueries;

  @BeforeEach
  public void setupData() {
    Game g = Game.newGame()
        .gameId("new-game-1583157438")
        .state(GameState.byCode(1))
        .configuration("{}");

    Boolean gameInserted = gameQueries
        .upsert(client, g);
    assertTrue(gameInserted);

    try (InputStream in = getClass().getResource("/data.json").openStream()) {
      List<GameMessage> gameMessages = jsonb.fromJson(in,
          new ArrayList<GameMessage>() {}.getClass().getGenericSuperclass());
      gameMessages.stream()
          .map(gm -> gm.getPlayer())
          .forEach(p -> {
            playerQueries
                .upsert(client, p);
          });
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error loading data file", e);
    }
  }

  @Test
  @Order(1)
  public void testPlayerPersistence() throws Exception {

    nap();

    Response response = given()
        .when().get("/api/leaderboard/");

    assertEquals(200, response.statusCode());

    String body = response.getBody().asString();
    assertNotNull(body);

    Leaderboard leaderboard = jsonb.fromJson(body, Leaderboard.class);

    List<Player> players = leaderboard.getLeaders();

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

    assertEquals(290, leaderboard.getDollars());
    assertEquals(11, leaderboard.getRights());
    assertEquals(3, leaderboard.getWrongs());
    assertEquals(3, leaderboard.getPlayers());

  }

  @Test
  @Order(2)
  public void testPlayerPersistenceWithRowCount() throws Exception {

    nap();

    Response response = given()
        .when().get("/api/leaderboard/?rowCount=1");

    assertEquals(200, response.statusCode());

    String body = response.getBody().asString();
    assertNotNull(body);

    Leaderboard leaderboard = jsonb.fromJson(body, Leaderboard.class);

    List<Player> players = leaderboard.getLeaders();
    Player aPlayer = players.get(0);
    assertNotNull(aPlayer);
    assertEquals(5, aPlayer.getPk());
    assertEquals("tom", aPlayer.getPlayerId());
    assertEquals("tom", aPlayer.getUsername());
    assertEquals(4, aPlayer.getRight());
    assertEquals(1, aPlayer.getWrong());
    assertEquals(100, aPlayer.getScore());

    assertEquals(290, leaderboard.getDollars());
    assertEquals(11, leaderboard.getRights());
    assertEquals(3, leaderboard.getWrongs());
    assertEquals(3, leaderboard.getPlayers());

  }

  @AfterEach
  public void clearRecords() {
    playerQueries.delete(client, 1);
    playerQueries.delete(client, 2);
    playerQueries.delete(client, 3);
    playerQueries.delete(client, 4);
    gameQueries.delete(client, 1);
  }


  private void nap() throws Exception {
    Thread.sleep(2000);
  }

}
