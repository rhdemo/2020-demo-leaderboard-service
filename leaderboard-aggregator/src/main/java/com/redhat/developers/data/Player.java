package com.redhat.developers.data;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.axle.pgclient.PgPool;
import io.vertx.axle.sqlclient.Row;
import io.vertx.axle.sqlclient.RowSet;
import io.vertx.axle.sqlclient.Tuple;

/**
 * Player
 */
public class Player{
  
  static Logger logger = Logger.getLogger(Player.class.getName());

  public String playerId;
  public String playerName;
  public int right; 
  public int wrong;
  public int score;
  public String clusterSource;
  public String avatar;
  public String gameId;

  public static CompletionStage<Optional<Player>> findById(PgPool client, String playerId){
    return client
    .preparedQuery("SELECT * from player where player_id=$1",
     Tuple.of(playerId))
     .thenApply(RowSet::iterator)
     .thenApply(iterator -> iterator.hasNext()?from(iterator.next()):null)
     .thenApply(o -> Optional.ofNullable(o))
     .exceptionally(e -> {
          logger.log(Level.SEVERE, "Error Finding player with id "+playerId, e);
          return null;
      });
  }

  public static CompletionStage<Boolean> upsert(PgPool client, Player player){
    CompletionStage<Boolean> pExist = playerExist(client, player.playerId);
    return pExist
          .thenCompose(b -> b?update(client, player):insert(client, player))
          .exceptionally(e -> {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return false;
          });
  }

  public static CompletionStage<Boolean> delete(PgPool client, String playerId){
    return client.preparedQuery(
        "DELETE FROM player WHERE player_id=$1",Tuple.of(playerId))
        .thenApply(pgRowset -> pgRowset.rowCount() == 1)
        .exceptionally(e -> {
          logger.log(Level.SEVERE, "Error deleting "+playerId, e);
          return false;
        });
  }

  private static Player from(Row row) {
    Player player = new Player();
    player.playerId = row.getString("player_id"); 
    player.playerName = row.getString("player_name"); 
    player.avatar = row.getString("player_avatar"); 
    player.clusterSource = row.getString("cluster_source"); 
    player.right = row.getInteger("guess_right"); 
    player.wrong = row.getInteger("guess_wrong"); 
    player.score = row.getInteger("guess_score"); 
    player.gameId = row.getString("game_id"); 
    return player;
  }

  private static CompletionStage<Boolean> playerExist(PgPool client, String playerId){
    return client
    .preparedQuery("SELECT player_name from player where player_id=$1",
     Tuple.of(playerId))
     .thenApply(RowSet::iterator)
     .thenApply(iterator -> iterator.hasNext()?true:false)
     .exceptionally(e -> {
          logger.log(Level.SEVERE, "Error checking if player exists "+playerId, e);
          return null;
      });
  }

  private static CompletionStage<Boolean> insert(PgPool client, Player player){
    logger.info("Inserting player with id "+player.playerId);
    return client.preparedQuery("INSERT INTO player"
        + "(player_id,player_name,guess_right,"
        + "guess_wrong,guess_score,cluster_source,"
        + "player_avatar,game_id)"
        + " VALUES ($1,$2,$3,$4,$5,$6,$7,$8)",playerParams(player))
        .thenApply(pgRowset ->pgRowset.rowCount() == 1 ? true: false)
        .exceptionally(e -> {
          logger.log(Level.SEVERE, "Error Inserting "+player.playerId, e);
          return false;
        });
  }

  private static CompletionStage<Boolean> update(PgPool client, Player player){
    logger.info("Updating player with id "+player.playerId);
    return client.preparedQuery("UPDATE player set "
        + "player_name=$2,guess_right=$3,"
        + "guess_wrong=$4,guess_score=$5,"
        + "cluster_source=$6,player_avatar=$7,"
        + "game_id=$8"
        + "WHERE player_id=$1",playerParams(player))
        .thenApply(pgRowset ->pgRowset.rowCount() == 1 ? true: false)
        .exceptionally(e -> {
          logger.log(Level.SEVERE, "Error Updating "+player.playerId, e);
          return false;
        }); 
  }

  
  private static Tuple playerParams(Player player) {

  return Tuple.tuple()
    .addString(player.playerId)
    .addString(player.playerName)
    .addInteger(player.right)
    .addInteger(player.wrong) 
    .addInteger(player.score)
    .addString(player.clusterSource)
    .addString(player.avatar)
    .addString(player.gameId);
  }
}