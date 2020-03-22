package com.redhat.developers.service;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import com.redhat.developers.sql.PlayerQueries;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import io.smallrye.mutiny.Multi;
import io.vertx.axle.pgclient.PgPool;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;

/**
 * LeaderboardBroadcasterService
 */
@ApplicationScoped
public class LeaderboardBroadcasterService {

  Logger logger =
      Logger.getLogger(LeaderboardBroadcasterService.class.getName());

  @Inject
  PgPool client;

  @Inject
  PlayerQueries playerQueries;

  @ConfigProperty(name = "rhdemo.leaderboard-broadcast.rowCount")
  int rowCount;

  @ConfigProperty(name = "rhdemo.leaderboard-broadcast.tickInterval")
  int tickInterval;

  @Outgoing("leaderboard-broadcast")
  public Multi<JsonArray> broadcastLeaderboard() {
    return Multi.createFrom()
        .ticks().every(Duration.ofSeconds(tickInterval))
        .on().overflow().drop()
        .onItem().produceCompletionStage(this::rankedPlayerList).concatenate();
  }

  /**
   * 
   * @param tick
   * @return
   */
  public CompletionStage<JsonArray> rankedPlayerList(long tick) {
    logger.log(FINE, "Sending message for tick {0} ", tick);
    AtomicReference<JsonArray> players = new AtomicReference<>();
    return playerQueries
        .rankPlayers(client, rowCount)
        .thenApply(p -> {
          try {
            players.set(new JsonArray(p));
            logger.log(FINE, "Broadcast Leaderboard data {0} ",
                Json.encode(players));
          } catch (Exception e) {
            logger.log(SEVERE, "Error building players json", e);
          }
          return players.get();
        })
        .exceptionally(e -> {
          logger.log(SEVERE, "Error retreiving player ranks ", e);
          return players.get();
        });
  }
}
