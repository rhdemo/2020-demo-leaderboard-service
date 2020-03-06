package com.redhat.developers.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Avatar;
import com.redhat.developers.data.Player;
import io.vertx.axle.pgclient.PgPool;
import io.vertx.axle.sqlclient.Row;
import io.vertx.axle.sqlclient.RowIterator;
import io.vertx.axle.sqlclient.RowSet;
import io.vertx.axle.sqlclient.Tuple;

/**
 * PlayerQueries
 */
@Singleton
public class PlayerQueries {

  static Logger logger = Logger.getLogger(PlayerQueries.class.getName());

  @Inject
  Jsonb jsonb;

  /**
   * 
   * @param client
   * @param playerId
   * @return
   */
  public CompletionStage<Optional<Player>> findById(PgPool client,
      String playerId, String gameId) {
    return client
        .preparedQuery("SELECT * from player where player_id=$1",
            Tuple.of(playerId, gameId))
        .thenApply(RowSet::iterator)
        .thenApply(
            iterator -> iterator.hasNext() ? from(iterator.next()) : null)
        .thenApply(o -> Optional.ofNullable(o))
        .exceptionally(e -> {
          logger.log(Level.SEVERE, "Error Finding player with id " + playerId,
              e);
          return null;
        });
  }

  /**
   * 
   * @param client
   * @param gameId
   * @return
   */
  public CompletionStage<List<Player>> rankPlayers(PgPool client) {
    return client
        .preparedQuery("SELECT * FROM player" +
            "ORDER BY guess_score DESC,"
            + "guess_right ASC,"
            + "guess_wrong DESC")
        .thenApply(this::playersList)
        .exceptionally(e -> {
          logger.log(Level.SEVERE,
              "Error ranking players for game", e);
          return null;
        });
  }

  /**
   * 
   * @param client
   * @param player
   * @return
   */
  public CompletionStage<Boolean> upsert(PgPool client, Player player) {
    CompletionStage<Boolean> pExist =
        playerExist(client, player.getId(), player.getGameId());
    return pExist
        .thenCompose(b -> b ? update(client, player) : insert(client, player))
        .exceptionally(e -> {
          logger.log(Level.SEVERE, e.getMessage(), e);
          return false;
        });
  }

  /**
   * 
   * @param client
   * @param playerId
   * @return
   */
  public CompletionStage<Boolean> delete(PgPool client,
      String playerId, String gameId) {
    return client.preparedQuery(
        "DELETE FROM player WHERE player_id=$1",
        Tuple.of(playerId, gameId))
        .thenApply(pgRowset -> pgRowset.rowCount() == 1)
        .exceptionally(e -> {
          logger.log(Level.SEVERE, "Error deleting " + playerId, e);
          return false;
        });
  }

  /**
   * 
   * @param row
   * @return
   */
  private Player from(Row row) {
    Player player = Player.newPlayer()
        .id(row.getString("player_id"))
        .username(row.getString("player_name"))
        .right(row.getInteger("guess_right"))
        .wrong(row.getInteger("guess_wrong"))
        .score(row.getInteger("guess_score"))
        .creationServer(row.getString("creation_server"))
        .scoringServer(row.getString("scoring_server"))
        .gameServer(row.getString("game_server"))
        .avatar(jsonb.fromJson(row.getString("player_avatar"), Avatar.class))
        .gameId(row.getString("game_id"));
    return player;
  }

  /**
   * 
   * @param rowSet
   * @return
   */
  private List<Player> playersList(RowSet<Row> rowSet) {
    List<Player> listOfPlayers = new ArrayList<>();
    RowIterator<Row> rowItr = rowSet.iterator();
    while (rowItr.hasNext()) {
      listOfPlayers.add(from(rowItr.next()));
    }
    return listOfPlayers;
  }

  /**
   * 
   * @param client
   * @param playerId
   * @return
   */
  private CompletionStage<Boolean> playerExist(PgPool client,
      String playerId, String gameId) {
    return client
        .preparedQuery(
            "SELECT player_name from player where player_id=$1",
            Tuple.of(playerId, gameId))
        .thenApply(RowSet::iterator)
        .thenApply(iterator -> iterator.hasNext() ? true : false)
        .exceptionally(e -> {
          logger.log(Level.SEVERE,
              "Error checking if player exists " + playerId, e);
          return null;
        });
  }

  /**
   * 
   * @param client
   * @param player
   * @return
   */
  private CompletionStage<Boolean> insert(PgPool client, Player player) {
    logger.info("Inserting player with id " + player.getId());
    return client.preparedQuery("INSERT INTO player"
        + "(player_id,player_name,guess_right,"
        + "guess_wrong,guess_score,creation_server,"
        + "game_server,scoring_server,"
        + "player_avatar,game_id)"
        + " VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10)", playerParams(player))
        .thenApply(pgRowset -> pgRowset.rowCount() == 1 ? true : false)
        .exceptionally(e -> {
          logger.log(Level.SEVERE, "Error Inserting " + player.getId(), e);
          return false;
        });
  }

  /**
   * 
   * @param client
   * @param player
   * @return
   */
  private CompletionStage<Boolean> update(PgPool client, Player player) {
    logger.info("Updating player with id " + player.getId());
    return client.preparedQuery("UPDATE player set "
        + "player_name=$2,guess_right=$3,"
        + "guess_wrong=$4,guess_score=$5,"
        + "creation_server=$6,game_server=$7,"
        + "scoring_server=$8,player_avatar=$9, game_id=$10"
        + "WHERE player_id=$1", playerParams(player))
        .thenApply(pgRowset -> pgRowset.rowCount() == 1 ? true : false)
        .exceptionally(e -> {
          logger.log(Level.SEVERE,
              "Error Updating " + player.getId() + " for game "
                  + player.getGameId(),
              e);
          return false;
        });
  }

  /**
   * 
   * @param player
   * @return
   */
  private Tuple playerParams(Player player) {
    return Tuple.tuple()
        .addString(player.getId())// Param Order 1
        .addString(player.getUsername()) // Param Order 2
        .addInteger(player.getRight()) // Param Order 3
        .addInteger(player.getWrong()) // Param Order 4
        .addInteger(player.getScore()) // Param Order 5
        .addString(player.getCreationServer()) // Param Order 6
        .addString(player.getGameServer()) // Param Order 7
        .addString(player.getScoringServer()) // Param Order 8
        .addString(jsonb.toJson(player.getAvatar())) // Param Order 9
        .addString(player.getGameId()); // Param Order 10
  }
}
