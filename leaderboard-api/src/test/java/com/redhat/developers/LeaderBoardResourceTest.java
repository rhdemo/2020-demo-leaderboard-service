package com.redhat.developers;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Game;
import com.redhat.developers.data.GameMessage;
import com.redhat.developers.data.GameState;
import com.redhat.developers.data.Player;
import com.redhat.developers.sql.GameQueries;
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
import io.restassured.response.Response;
import io.vertx.core.Vertx;
import io.vertx.mutiny.pgclient.PgPool;

/**
 * LeaderBoardResourceTest
 */
@QuarkusTest
@QuarkusTestResource(APIQuarkusTestEnv.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
public class LeaderBoardResourceTest {

  static final Logger logger =
      Logger.getLogger(LeaderBoardResourceTest.class.getName());

  @Inject
  Jsonb jsonb;

  @Inject
  PgPool client;

  @Inject
  PlayerQueries playerQueries;

  @Inject
  GameQueries gameQueries;

  Vertx vertx = Vertx.vertx();

  AtomicInteger testRecordCount = new AtomicInteger();

  @BeforeAll
  public void setupData() {
    Game g = Game.newGame()
        .gameId("new-game-1583157438")
        .state(GameState.byCode(1))
        .configuration("{}");

    gameQueries
        .upsert(client, g)
        .await().atMost(Duration.ofSeconds(10));

    try (InputStream in = getClass().getResource("/data.json").openStream()) {
      List<GameMessage> gameMessages = jsonb.fromJson(in,
          new ArrayList<GameMessage>() {}.getClass().getGenericSuperclass());
      gameMessages.stream()
          .map(gm -> gm.getPlayer())
          .forEach(p -> {
            playerQueries
                .upsert(client, p)
                .onItem()
                .apply(b -> b ? testRecordCount.incrementAndGet() : 0);
          });
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error loading data file", e);
    }
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

    Response response = given()
        .when().get("/api/leaderboard/");

    assertEquals(200, response.statusCode());

    String body = response.getBody().asString();
    assertNotNull(body);

    List<Player> players = jsonb.fromJson(body,
        new ArrayList<Player>() {}.getClass().getGenericSuperclass());

    assertNotNull(players);
    assertEquals(3, players.size());
    Player aPlayer = players.get(0);
    assertNotNull(aPlayer);
    assertEquals(1, aPlayer.getId());
    assertEquals("tom", aPlayer.getPlayerId());
    assertEquals("tom", aPlayer.getUsername());
    assertEquals(4, aPlayer.getRight());
    assertEquals(1, aPlayer.getWrong());
    assertEquals(100, aPlayer.getScore());

    aPlayer = players.get(1);
    assertNotNull(aPlayer);
    assertNotNull(aPlayer);
    assertEquals(3, aPlayer.getId());
    assertEquals("jerry", aPlayer.getPlayerId());
    assertEquals("jerry", aPlayer.getUsername());
    assertEquals(4, aPlayer.getRight());
    assertEquals(1, aPlayer.getWrong());
    assertEquals(95, aPlayer.getScore());

    aPlayer = players.get(2);
    assertEquals(4, aPlayer.getId());
    assertEquals("Winne", aPlayer.getPlayerId());
    assertEquals("Winne", aPlayer.getUsername());
    assertEquals(3, aPlayer.getRight());
    assertEquals(1, aPlayer.getWrong());
    assertEquals(95, aPlayer.getScore());

  }

  @Test
  @Order(2)
  public void testPlayerPersistenceWithRowCount() throws Exception {

    nap();

    // Just to make sure the records are sent to Kafka
    Awaitility.await()
        .atMost(Duration.ofSeconds(10))
        .untilAtomic(this.testRecordCount,
            Is.is(Matchers.greaterThanOrEqualTo(3)));

    Response response = given()
        .when().get("/api/leaderboard/?rowCount=1");

    assertEquals(200, response.statusCode());

    String body = response.getBody().asString();
    assertNotNull(body);

    List<Player> players = jsonb.fromJson(body,
        new ArrayList<Player>() {}.getClass().getGenericSuperclass());

    Player aPlayer = players.get(0);
    assertNotNull(aPlayer);
    assertEquals(1, aPlayer.getId());
    assertEquals("tom", aPlayer.getPlayerId());
    assertEquals("tom", aPlayer.getUsername());
    assertEquals(4, aPlayer.getRight());
    assertEquals(1, aPlayer.getWrong());
    assertEquals(100, aPlayer.getScore());

  }

  @AfterAll
  public void clearRecords() {
    gameQueries.delete(client, 1);
    playerQueries.delete(client, 1);
    playerQueries.delete(client, 2);
    playerQueries.delete(client, 3);
    playerQueries.delete(client, 4);
  }


  private void nap() throws Exception {
    Thread.sleep(2000);
  }

}
