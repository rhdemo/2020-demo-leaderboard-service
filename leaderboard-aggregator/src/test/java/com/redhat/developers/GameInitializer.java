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

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Game;
import com.redhat.developers.data.GameMessage;
import com.redhat.developers.data.GameState;
import com.redhat.developers.sql.GameQueries;
import io.vertx.core.Vertx;
import io.vertx.mutiny.pgclient.PgPool;

/**
 * GameInitService
 */
@Singleton
public class GameInitializer {

  Logger logger = Logger.getLogger("GameInitializer");

  @Inject
  Jsonb jsonb;

  @Inject
  GameQueries gameQueries;

  @Inject
  PgPool client;
  Game game;

  Vertx vertx = Vertx.vertx();

  List<GameMessage> gameMessages;

  @PostConstruct
  void init() throws Exception {
    Game g = Game.newGame()
        .gameId("new-game-1583157438")
        .state(GameState.byCode(1))
        .configuration("{}");

    gameQueries
        .upsert(client, g)
        .await().atMost(Duration.ofSeconds(10));

  }

  public Optional<Game> gameExist() {
    Optional<List<Game>> games = gameQueries
        .findAll(client)
        .await().asOptional().atMost(Duration.ofSeconds(10));
    this.game = games.get().get(0);
    return Optional.ofNullable(games.get().get(0));
  }

  public void deleteGame() {
    gameQueries.delete(client, this.game.getId());
  }
}