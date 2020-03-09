package com.redhat.developers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Game;
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

  @BeforeEach
  public void chekEnv() {
    assertNotNull("quarkus.datasource.url");
    String postgresqlURL = String.format(QuarkusTestEnv.JDBC_URL,
        QuarkusTestEnv.postgreSQL.getContainerIpAddress(),
        QuarkusTestEnv.postgreSQL.getMappedPort(5432));
    assertEquals(postgresqlURL, System.getProperty("quarkus.datasource.url"));
  }

  @Test
  @Order(1)
  public void testGameAdd() {
    Game game = Game.newGame()
        .id("id0001").state("active")
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
        .id("id0001").state("paused")
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
        .id("id0001").state("paused")
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
        .state("active")
        .config("{}")
        .date(someGMTDateTime);

    given()
        .contentType(ContentType.JSON)
        .body(jsonb.toJson(game2))
        .when().post("/api/game/save")
        .then()
        .statusCode(202)
        .body(is(""));

    // Check we get back active game
    given()
        .when()
        .get("/api/game/id0002")
        .then()
        .statusCode(200)
        .body(is(jsonb.toJson(game2)));
  }


  @Test
  @Order(5)
  public void testGameDelete() {
    given()
        .when()
        .delete("/api/game/id0001")
        .then()
        .statusCode(204)
        .body(is(""));
  }

  @Test
  @Order(6)
  public void testGameNobody() {
    given()
        .when()
        .get("/api/game/id0001")
        .then()
        .statusCode(404)
        .body(is(""));
  }

}
