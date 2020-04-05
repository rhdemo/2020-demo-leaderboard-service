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

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Player;
import com.redhat.developers.sql.PlayerQueries;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import io.vertx.mutiny.pgclient.PgPool;

/**
 * PlayerPersistenceService
 */
@ApplicationScoped
public class PlayerPersistenceService {

  Logger logger = Logger.getLogger(PlayerPersistenceService.class.getName());

  @Inject
  Jsonb jsonb;

  @Inject
  PgPool client;

  @Inject
  PlayerQueries playerQueries;

  @Incoming("leaderboard-persist-to-db")
  public void handleScores(Player player) {
    final Instant startTime = Instant.now();
    logger.log(Level.INFO,
        "Saving Player  {0} ", player.getPlayerId());
    playerQueries
        .upsert(client, player).subscribe().with(b -> {
          if (b) {
            final Instant endTime = Instant.now();
            logger.log(Level.INFO,
                "Player {0} Saved in {1} ms",
                new Object[] {player.getPlayerId(),
                    Duration.between(startTime, endTime).toMillis()});
          } else {
            logger.log(Level.INFO,
                "Unable to save Player {0} ", player.getPlayerId());
          }
        }, e -> {
          logger.log(Level.SEVERE, "Error saving Player {0}",
              player.getPlayerId());
        });
  }
}
