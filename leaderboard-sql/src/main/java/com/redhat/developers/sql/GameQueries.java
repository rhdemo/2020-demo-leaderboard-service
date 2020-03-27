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
package com.redhat.developers.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import com.redhat.developers.data.Game;
import com.redhat.developers.data.GameState;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowIterator;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

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
  public Uni<List<Game>> findAll(PgPool client) {
    logger.info("Find All");
    return client
        .preparedQuery("SELECT * from games ORDER BY game_date DESC")
        .onFailure()
        .invoke(e -> logger.log(Level.SEVERE, "Error retriving all games ",
            e))
        .onItem().apply(this::games);
  }

  /**
   * 
   * @param client
   * @param id
   * @return
   */
  public Uni<Optional<Game>> findById(PgPool client,
      int id) {
    logger.info("Finding game with id " + id);
    return client
        .preparedQuery("SELECT * from games where id=$1"
            + "ORDER BY game_date DESC",
            Tuple.of(id))
        .onFailure().invoke(e -> {
          logger.log(Level.SEVERE,
              "Error Finding game with id " + id, e);
        })
        .onItem().apply(RowSet::iterator)
        .onItem()
        .apply(iterator -> iterator.hasNext() ? from(iterator.next()) : null)
        .onItem().apply(g -> Optional.ofNullable(g));
  }

  /**
   * 
   * @param client
   * @return
   */
  public Uni<Optional<Game>> findActiveGame(PgPool client) {
    logger.info("Finding Acive game");
    return client
        .preparedQuery("SELECT * from games where game_state='active'"
            + " ORDER BY game_date DESC"
            + " FETCH FIRST 1 ROW ONLY")
        .onFailure().invoke(e -> {
          logger.log(Level.SEVERE,
              "Error Finding active game ", e);
        })
        .onItem().apply(RowSet::iterator)
        .onItem()
        .apply(iterator -> iterator.hasNext() ? from(iterator.next()) : null)
        .onItem().apply(g -> Optional.ofNullable(g));
  }

  public Uni<Boolean> delete(PgPool client, int id) {
    logger.info("Deleting game with id " + id);
    return client.preparedQuery(
        "DELETE FROM games WHERE id=$1",
        Tuple.of(id))
        .onFailure().invoke(e -> {
          logger.log(Level.SEVERE, "Error deleting game with id " + id,
              e);
        })
        .onItem().apply(rs -> rs.rowCount() == 1);
  }

  /**
   * 
   * @param client
   * @param game
   * @return
   */
  public Uni<Boolean> upsert(PgPool client, Game game) {
    logger.info("Upserting game with id " + game.getId());
    return client
        .preparedQuery("INSERT INTO games(game_id,game_config,game_state)"
            + " VALUES ($1,$2,$3)"
            + " ON CONFLICT (game_id)"
            + " DO"
            + "   UPDATE set "
            + "     game_config=$2,"
            + "     game_state=$3",
            gameTuple(game))
        .onFailure().invoke(e -> logger.log(Level.SEVERE,
            "Error Upserting Game with id " + game.getId(), e))
        .onItem().apply(rs -> rs.rowCount() == 1 ? true : false);
  }

  /**
   * 
   * @param row
   * @return
   */
  private Game from(Row row) {
    return Game.newGame()
        .id(row.getLong("id"))
        .gameId(row.getString("game_id"))
        .configuration(row.getString("game_config"))
        .date(row.getOffsetDateTime("game_date"))
        .state(GameState.byCode(row.getInteger("game_state")));
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
   * @param game
   * @return
   */
  private Tuple gameTuple(Game game) {
    return Tuple.tuple()
        .addString(game.getGameId())// Param Order 1
        .addString(game.getConfiguration()) // Param Order 2
        .addInteger(GameState.byCodeString(game.getState())); // Param Order 3
  }
}
