/*-
 * #%L
 * Leaderboard Broadcast
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
import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Player;
import com.redhat.developers.sql.PlayerQueries;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import io.smallrye.mutiny.Multi;

/**
 * LeaderboardBroadcasterService
 */
@ApplicationScoped
public class LeaderboardBroadcasterService {

  Logger logger =
      Logger.getLogger(LeaderboardBroadcasterService.class.getName());

  @Inject
  Jsonb jsonb;

  @Inject
  PlayerQueries playerQueries;

  @ConfigProperty(name = "rhdemo.leaderboard-broadcast.rowCount")
  int rowCount;

  @ConfigProperty(name = "rhdemo.leaderboard-broadcast.tickInterval")
  int tickInterval;

  @Outgoing("leaderboard-broadcast")
  public Multi<String> broadcastLeaderboard() {
    return Multi.createFrom()
        .ticks().every(Duration.ofSeconds(tickInterval))
        .on().overflow().drop()
        .onItem()
        .apply(this::rankedPlayerList);
  }

  /**
   * 
   * @param tick
   * @return
   */
  public String rankedPlayerList(long tick) {
    logger.log(FINE, "Sending message for tick {0} ", tick);
    List<Player> players = playerQueries
        .rankPlayers(rowCount);
    return jsonb.toJson(players);
  }
}
̦