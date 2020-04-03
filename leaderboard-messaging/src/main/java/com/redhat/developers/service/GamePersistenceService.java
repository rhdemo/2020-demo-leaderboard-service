/*-
 * #%L
 * Leaderboard Reactive Messaging
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
package com.redhat.developers.service;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Game;
import com.redhat.developers.data.GameMessage;
import com.redhat.developers.sql.GameQueries;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import io.vertx.mutiny.pgclient.PgPool;

/**
 * GamePersistenceService
 */
@ApplicationScoped
public class GamePersistenceService {

  Logger logger = Logger.getLogger(GamePersistenceService.class.getName());

  @Inject
  Jsonb jsonb;

  @Inject
  PgPool client;

  @Inject
  GameQueries gameQueries;

  @Incoming("game-state")
  public CompletionStage<Boolean> saveGame(String payload) {
    // JsonObject raw = new JsonObject(map);
    // logger.log(FINE, "Received Game State Payload {0} ", raw.encode());
    // GameMessage gameMessage = raw.mapTo(GameMessage.class)
    GameMessage gameMessage = jsonb.fromJson(payload, GameMessage.class);
    if (gameMessage.getType() != null
        && ("reset-game".equals(gameMessage.getType())
            || "game".equals(gameMessage.getType()))) {
      Game game = gameMessage.getGame();
      logger.log(FINE, "Saving game {0} ", game.getPk());
      return gameQueries.upsert(client, game)
          .subscribe().asCompletionStage();
    } else {
      logger.log(INFO,
          "Game is not of type 'game' or 'reset-game', save skipped");
    }
    return null;
  }

}
