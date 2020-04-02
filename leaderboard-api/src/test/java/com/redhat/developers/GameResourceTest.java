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
import static org.hamcrest.CoreMatchers.is;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Game;
import com.redhat.developers.data.GameState;
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
@QuarkusTestResource(APIQuarkusTestEnv.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameResourceTest {

  @Inject
  Jsonb jsonb;


  @Test
  @Order(1)
  public void testGameAdd() throws Exception {
    Game game = Game.newGame()
        .gameId("id0001")
        .state(GameState.byCode(1))
        .configuration("{}");
    given()
        .contentType(ContentType.JSON)
        .body(jsonb.toJson(game))
        .when().post("/api/game/save")
        .then()
        .statusCode(202)
        .body(is(""));

    // Check if its properly updated
    given()
        .when().get("/api/game/1")
        .then()
        .statusCode(200)
        .body(is(jsonb.toJson(game.id(1))));
  }

  @Test
  @Order(2)
  public void testGameUpdate() throws Exception {
    Game game = Game.newGame()
        .gameId("id0001")
        .state(GameState.byCode(4))
        .configuration("{}");

    given()
        .contentType(ContentType.JSON)
        .body(jsonb.toJson(game))
        .when().post("/api/game/save")
        .then()
        .statusCode(202)
        .body(is(""));

    // Check if its properly updated
    given()
        .when().get("/api/game/1")
        .then()
        .statusCode(200)
        .body(is(jsonb.toJson(game.id(1))));
  }

  @Test
  @Order(3)
  public void testGameFind() throws Exception {
    Game game = Game.newGame()
        .gameId("id0001")
        .state(GameState.byCode(4))
        .configuration("{}");

    // Check if its properly updated
    given()
        .when()
        .get("/api/game/1")
        .then()
        .statusCode(200)
        .body(is(jsonb.toJson(game.id(1))));
  }

  @Test
  @Order(4)
  public void testGameFindActive() throws Exception {

    Game game2 = Game.newGame()
        .gameId("id0002")
        .state(GameState.byCode(1))
        .configuration("{}");

    Game game3 = Game.newGame()
        .gameId("id0003")
        .state(GameState.byCode(1))
        .configuration("{}");

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
        .get("/api/game/active")
        .then()
        .statusCode(200)
        .body(is(jsonb.toJson(game3.id(4))));
  }

  @Test
  @Order(5)
  public void testGameFindAll() throws Exception {

    // Check we get back active game sorted by time
    given()
        .when()
        .get("/api/game/all")
        .then()
        .assertThat()
        .contentType(ContentType.JSON)
        .body("size()", is(3));
  }

  @Test
  @Order(6)
  public void testGameDelete() throws Exception {
    given()
        .when()
        .delete("/api/game/1")
        .then()
        .statusCode(204)
        .body(is(""));
  }

  @Test
  @Order(7)
  public void testNoGame() throws Exception {
    given()
        .when()
        .get("/api/game/1")
        .then()
        .statusCode(404)
        .body(is(""));
  }

}
