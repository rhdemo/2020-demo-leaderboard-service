package com.redhat.developers.service;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Player;
import com.redhat.developers.sql.PlayerQueries;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import io.vertx.axle.pgclient.PgPool;

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
  public void saveToDB(String playerJson) {
    try {
      logger.info("Saving Player:" + playerJson);
      Player player = jsonb.fromJson(playerJson, Player.class);
      playerQueries.upsert(client, player);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error saving player info " + playerJson, e);
    }
  }

  /**
   * 
   * @param gameId
   * @return
   */
  public CompletionStage<List<Player>> rankedPlayerList(String gameId) {
    return playerQueries.rankPlayers(client, gameId);
  }
}
