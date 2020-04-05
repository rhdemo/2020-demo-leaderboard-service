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

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Game;
import com.redhat.developers.data.GameMessage;
import com.redhat.developers.sql.GameQueries;
import org.eclipse.microprofile.reactive.messaging.Incoming;

/**
 * GamePersistenceService
 */
@ApplicationScoped
public class GamePersistenceService {

  Logger logger = Logger.getLogger(GamePersistenceService.class.getName());

  @Inject
  Jsonb jsonb;

  @Inject
  @Named("gamedb")
  Connection dbConn;

  @Inject
  GameQueries gameQueries;

  @Incoming("game-state")
  public void saveGame(String payload) {
    GameMessage gameMessage = jsonb.fromJson(payload, GameMessage.class);
    if (gameMessage.getType() != null
        && ("reset-game".equals(gameMessage.getType())
            || "game".equals(gameMessage.getType()))) {
      Game game = gameMessage.getGame();
      logger.log(Level.INFO, "Saving game {0} ", game.getPk());
      boolean isInserted = gameQueries.upsert(dbConn, game);

      if (isInserted) {
        logger.log(Level.INFO,
            "Saved Game {0} sucessfully ", game.getGameId());
      } else {
        logger.log(Level.INFO,
            "Unable to save Game {0} ", game.getGameId());
      }
    }
  }

}
