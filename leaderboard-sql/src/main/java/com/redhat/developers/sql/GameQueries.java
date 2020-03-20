package com.redhat.developers.sql;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import com.redhat.developers.data.Game;
import io.vertx.axle.pgclient.PgPool;
import io.vertx.axle.sqlclient.Row;
import io.vertx.axle.sqlclient.RowIterator;
import io.vertx.axle.sqlclient.RowSet;
import io.vertx.axle.sqlclient.Tuple;

/**
 * GameQueries
 */
@ApplicationScoped
public class GameQueries {

  static Logger logger = Logger.getLogger(GameQueries.class.getName());


  /**
   * 
   * @param client
   * @return
   */
  public CompletionStage<List<Game>> findAll(PgPool client) {
    return client
        .preparedQuery("SELECT * from games ORDER BY game_date DESC")
        .thenApply(this::games)
        .exceptionally(e -> {
          logger.log(Level.SEVERE, "Error retriving all games ",
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
  public CompletionStage<Optional<Game>> findById(PgPool client,
      String gameId) {
    return client
        .preparedQuery("SELECT * from games where game_id=$1"
            + "ORDER BY game_date DESC",
            Tuple.of(gameId))
        .thenApply(RowSet::iterator)
        .thenApply(
            iterator -> iterator.hasNext() ? from(iterator.next()) : null)
        .thenApply(o -> Optional.ofNullable(o))
        .exceptionally(e -> {
          logger.log(Level.SEVERE, "Error Finding game with id " + gameId,
              e);
          return null;
        });
  }

  /**
   * 
   * @param client
   * @return
   */
  public CompletionStage<Optional<Game>> findActiveGame(PgPool client) {
    return client
        .preparedQuery("SELECT * from games where game_state='active'"
            + " ORDER BY game_date DESC"
            + " FETCH FIRST 1 ROW ONLY")
        .thenApply(RowSet::iterator)
        .thenApply(
            iterator -> iterator.hasNext() ? from(iterator.next()) : null)
        .thenApply(o -> Optional.ofNullable(o))
        .exceptionally(e -> {
          logger.log(Level.SEVERE,
              "Error Finding active game", e);
          return null;
        });
  }

  /**
   * 
   * @param client
   * @param state
   * @return
   */
  public CompletionStage<List<Game>> gamesByState(PgPool client,
      String state) {
    return client
        .preparedQuery("SELECT * from games where game_state=$1"
            + "ORDER BY game_date ASC",
            Tuple.of(state))
        .thenApply(this::games)
        .exceptionally(e -> {
          logger.log(Level.SEVERE,
              "Error games by state " + state, e);
          return null;
        });
  }

  /**
   * 
   * @param client
   * @param game
   * @return
   */
  public CompletionStage<Boolean> upsert(PgPool client, Game game) {
    CompletionStage<Boolean> gameExist =
        gameExists(client, game.getId());
    setGameDateTimestamp(game);
    return gameExist
        .thenCompose(b -> b ? update(client, game) : insert(client, game))
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
  public CompletionStage<Boolean> delete(PgPool client, String gameId) {
    return client.preparedQuery(
        "DELETE FROM games WHERE game_id=$1",
        Tuple.of(gameId))
        .thenApply(pgRowset -> pgRowset.rowCount() == 1)
        .exceptionally(e -> {
          logger.log(Level.SEVERE, "Error deleting game with id " + gameId,
              e);
          return false;
        });
  }


  /**
   * 
   * @param row
   * @return
   */
  private Game from(Row row) {
    return Game.newGame()
        .id(row.getString("game_id"))
        .config(row.getString("game_config"))
        .date(row.getOffsetDateTime("game_date"))
        .state(row.getString("game_state"));
  }

  /**
   * 
   * @param rowSet
   * @return
   */
  private List<Game> games(RowSet<Row> rowSet) {
    List<Game> listOfGames = new ArrayList<>();
    RowIterator<Row> rowItr = rowSet.iterator();
    while (rowItr.hasNext()) {
      listOfGames.add(from(rowItr.next()));
    }
    return listOfGames;
  }

  /**
   * 
   * @param client
   * @param gameId
   * @return
   */
  private CompletionStage<Boolean> gameExists(PgPool client, String gameId) {
    return client
        .preparedQuery(
            "SELECT game_state from games where game_id=$1",
            Tuple.of(gameId))
        .thenApply(RowSet::iterator)
        .thenApply(iterator -> iterator.hasNext() ? true : false)
        .exceptionally(e -> {
          logger.log(Level.SEVERE,
              "Error checking if game exists " + gameId, e);
          return null;
        });
  }

  /**
   * 
   * @param client
   * @param game
   * @return
   */
  private CompletionStage<Boolean> insert(PgPool client, Game game) {
    logger.info("Inserting game with id " + game.getId());
    return client.preparedQuery("INSERT INTO games"
        + "(game_id,game_config,game_date,game_state)"
        + " VALUES ($1,$2,$3,$4)", gameParams(game))
        .thenApply(pgRowset -> pgRowset.rowCount() == 1 ? true : false)
        .exceptionally(e -> {
          logger.log(Level.SEVERE,
              "Error Inserting Game with id " + game.getId(), e);
          return false;
        });
  }

  /**
   * 
   * @param client
   * @param game
   * @return
   */
  private CompletionStage<Boolean> update(PgPool client, Game game) {
    logger.info("Updating Game with id " + game.getId());
    return client.preparedQuery("UPDATE games set "
        + "game_config=$2,game_date=$3,"
        + "game_state=$4"
        + "WHERE game_id=$1", gameParams(game))
        .thenApply(pgRowset -> pgRowset.rowCount() == 1 ? true : false)
        .exceptionally(e -> {
          logger.log(Level.SEVERE,
              "Error Updating game with id " + game.getId(), e);
          return false;
        });
  }

  /**
   * 
   * @param game
   * @return
   */
  private Tuple gameParams(Game game) {
    return Tuple.tuple()
        .addString(game.getId())// Param Order 1
        .addString(game.getConfig()) // Param Order 2
        .addOffsetDateTime(now()) // Param Order 3
        .addString(game.getState().toString()); // Param Order 4
  }


  private void setGameDateTimestamp(Game game) {
    game.date(now());
  }

  private OffsetDateTime now() {
    Calendar calendar = Calendar.getInstance();
    return OffsetDateTime.of(
        LocalDateTime.of(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND)),
        ZoneOffset.ofHoursMinutes(0, 0));
  }
}
