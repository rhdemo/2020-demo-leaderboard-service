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
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Avatar;
import com.redhat.developers.data.Player;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowIterator;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

/**
 * PlayerQueries
 */
@ApplicationScoped
public class PlayerQueries {

  static Logger logger = Logger.getLogger(PlayerQueries.class.getName());

  @Inject
  Jsonb jsonb;

  /**
   * 
   * @param client
   * @param id
   * @return
   */
  public Uni<Optional<Player>> findById(PgPool client, long id) {
    return client
        .preparedQuery(
            "SELECT * from players where id=$1",
            Tuple.of(id))
        .onFailure().invoke(e -> {
          logger.log(Level.SEVERE, "Error Finding player with id " + id,
              e);
        })
        .onItem().apply(RowSet::iterator)
        .onItem()
        .apply(iterator -> iterator.hasNext() ? from(iterator.next()) : null)
        .onItem().apply(p -> Optional.ofNullable(p));

  }

  /**
   * 
   * @param client
   * @param rowCount
   * @return
   */
  public Uni<List<Player>> rankPlayers(PgPool client,
      int rowCount) {
    return client
        .preparedQuery("SELECT p.* FROM players p"
            + " WHERE p.game_id=(SELECT g.game_id from games g ORDER BY g.game_date DESC FETCH FIRST 1 ROW ONLY)"
            + " ORDER BY p.guess_score DESC,"
            + " p.guess_right DESC,"
            + " p.guess_wrong ASC"
            + " FETCH FIRST $1 ROW ONLY", Tuple.of(rowCount))
        .onFailure().invoke(e -> {
          logger.log(Level.SEVERE,
              "Error ranking players for game", e);
        })
        .onItem().apply(this::playersList);
  }


  /**
   * 
   * @param client
   * @param player
   * @return
   */
  public Uni<Boolean> upsert(PgPool client, Player player) {
    logger.info("Upserting player with id " + player.getPk());
    return client.preparedQuery("INSERT INTO players"
        + "(player_id,player_name,guess_right,"
        + "guess_wrong,guess_score,creation_server,"
        + "game_server,scoring_server,"
        + "player_avatar,game_id)"
        + " VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10)"
        + " ON CONFLICT (player_id) WHERE game_id=$10"
        + "  DO "
        + "   UPDATE  set "
        + "     player_name=$2,guess_right=$3,"
        + "     guess_wrong=$4,guess_score=$5,"
        + "     creation_server=$6,game_server=$7,"
        + "     scoring_server=$8,player_avatar=$9",
        playerParams(player))
        .onFailure().invoke(e -> {
          logger.log(Level.SEVERE, "Error Inserting " + player.getPk(), e);
        })
        .onItem().apply(pgRowset -> pgRowset.rowCount() == 1 ? true : false);
  }

  /**
   * 
   * @param client
   * @param id
   * @return
   */
  public Uni<Boolean> delete(PgPool client, long id) {
    return client.preparedQuery(
        "DELETE FROM players WHERE id=$1",
        Tuple.of(id))
        .onFailure().invoke(e -> {
          logger.log(Level.SEVERE, "Error deleting player with " + id, e);
        })
        .onItem().apply(pgRowset -> pgRowset.rowCount() == 1);

  }

  /**
   * 
   * @param row
   * @return
   */
  private Player from(Row row) {
    Player player = Player.newPlayer()
        .pk(row.getLong("id"))
        .playerId(row.getString("player_id"))
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
   * @param player
   * @return
   */
  private Tuple playerParams(Player player) {
    return Tuple.tuple()
        .addString(player.getPlayerId())// Param Order 1
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
