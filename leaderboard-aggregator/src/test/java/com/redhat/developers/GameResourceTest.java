package com.redhat.developers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Game;
import com.redhat.developers.data.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * GameResourceTest
 */
@QuarkusTest
@QuarkusTestResource(QuarkusTestEnv.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameResourceTest {

  @Inject
  Jsonb jsonb;

  // Some GMT time
  OffsetDateTime someGMTDateTime = OffsetDateTime.of(
      LocalDateTime.of(2020, Month.MARCH, 9, 18, 01, 00),
      ZoneOffset.ofHoursMinutes(0, 0));

  OffsetDateTime anotherGMTDateTime = OffsetDateTime.of(
      LocalDateTime.of(2020, Month.MARCH, 9, 18, 04, 00),
      ZoneOffset.ofHoursMinutes(0, 0));

  @BeforeEach
  public void chekEnv() {
    assertNotNull("quarkus.datasource.url");
  }

  @Test
  @Order(1)
  public void testGameAdd() {
    Game game = Game.newGame()
        .id("id0001").state(GameState.active)
        .config("{}")
        .date(someGMTDateTime);
    given()
        .contentType(ContentType.JSON)
        .body(jsonb.toJson(game))
        .when().post("/api/game/save")
        .then()
        .statusCode(202)
        .body(is(""));

    // Check if its properly updated
    given()
        .when().get("/api/game/id0001")
        .then()
        .statusCode(200)
        .body(is(jsonb.toJson(game)));
  }

  @Test
  @Order(2)
  public void testGameUpdate() {
    Game game = Game.newGame()
        .id("id0001").state(GameState.paused)
        .config("{}")
        .date(someGMTDateTime);

    given()
        .contentType(ContentType.JSON)
        .body(jsonb.toJson(game))
        .when().post("/api/game/save")
        .then()
        .statusCode(202)
        .body(is(""));

    // Check if its properly updated
    given()
        .when().get("/api/game/id0001")
        .then()
        .statusCode(200)
        .body(is(jsonb.toJson(game)));
  }

  @Test
  @Order(3)
  public void testGameFind() {
    Game game = Game.newGame()
        .id("id0001").state(GameState.paused)
        .config("{}")
        .date(someGMTDateTime);

    // Check if its properly updated
    given()
        .when()
        .get("/api/game/id0001")
        .then()
        .statusCode(200)
        .body(is(jsonb.toJson(game)));
  }

  @Test
  @Order(4)
  public void testGameFindActive() {

    Game game2 = Game.newGame()
        .id("id0002")
        .state(GameState.active)
        .config("{}")
        .date(someGMTDateTime);

    Game game3 = Game.newGame()
        .id("id0003")
        .state(GameState.active)
        .config("{}")
        .date(anotherGMTDateTime);

    given()
        .contentType(ContentType.JSON)
        .body(jsonb.toJson(game2))
        .when().post("/api/game/save")
        .then()
        .statusCode(202)
        .body(is(""));

    given()
        .contentType(ContentType.JSON)
        .body(jsonb.toJson(game3))
        .when().post("/api/game/save")
        .then()
        .statusCode(202)
        .body(is(""));

    // Check we get back active game sorted by time
    given()
        .when()
        .get("/api/game/id0002")
        .then()
        .statusCode(200)
        .body(is(jsonb.toJson(game2)));
  }

  @Test
  @Order(5)
  public void testGameFindAll() {

    // Game game2 = Game.newGame()
    // .id("id0002")
    // .state(GameState.active)
    // .config("{}")
    // .date(someGMTDateTime);

    // Game game3 = Game.newGame()
    // .id("id0003")
    // .state(GameState.active)
    // .config("{}")
    // .date(anotherGMTDateTime);


    // Check we get back active game sorted by time
    given()
        .when()
        .get("/api/game/all")
        .then()
        .assertThat()
        .contentType(ContentType.JSON)
        .body("size()", is(6));
  }

  @Test
  @Order(6)
  public void testGameDelete() {
    given()
        .when()
        .delete("/api/game/id0001")
        .then()
        .statusCode(204)
        .body(is(""));
  }

  @Test
  @Order(7)
  public void testGameNobody() {
    given()
        .when()
        .get("/api/game/id0001")
        .then()
        .statusCode(404)
        .body(is(""));
  }

}
