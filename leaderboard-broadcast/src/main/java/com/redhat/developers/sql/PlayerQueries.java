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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Avatar;
import com.redhat.developers.data.GameTotal;
import com.redhat.developers.data.Player;
import io.agroal.api.AgroalDataSource;

/**
 * PlayerQueries
 */
@ApplicationScoped
public class PlayerQueries {

  static Logger logger = Logger.getLogger(PlayerQueries.class.getName());

  @Inject
  Jsonb jsonb;

  @Inject
  AgroalDataSource dataSource;

  public List<Player> rankPlayers(int rowCount) {
    List<Player> players = new ArrayList<>();
    try (Connection dbConn = dataSource.getConnection();
        PreparedStatement pst =
            dbConn.prepareStatement("SELECT p.* FROM players p"
                + " WHERE p.game_id=(SELECT g.game_id from games g ORDER BY g.game_date DESC FETCH FIRST 1 ROW ONLY)"
                + " ORDER BY p.guess_score DESC,"
                + " p.guess_right DESC,"
                + " p.guess_wrong ASC"
                + " FETCH FIRST ? ROW ONLY")) {
      pst.setLong(1, rowCount);
      ResultSet rs = pst.executeQuery();
      players = this.playersList(rs);
      pst.close();
    } catch (SQLException e) {
      logger.log(Level.SEVERE,
          "Error ranking players for game", e);
    }
    return players;
  }

  /**
   * 
   * @return
   */
  public Optional<GameTotal> gameTotals() {
    GameTotal gameTotal = null;
    try (Connection dbConn = dataSource.getConnection();
        PreparedStatement pst =
            dbConn.prepareStatement("SELECT COUNT(*) as total_players,"
                + " SUM(p.guess_right) as total_rights,"
                + " SUM(p.guess_wrong) as total_wrongs,"
                + " SUM(p.guess_score) as total_dollars"
                + " FROM players p"
                + " WHERE p.game_id=(SELECT g.game_id from games g ORDER BY g.game_date DESC FETCH FIRST 1 ROW ONLY)")) {
      ResultSet rs = pst.executeQuery();
      if (rs.next()) {
        gameTotal = gameTotal(rs);
      }
      pst.close();
    } catch (SQLException e) {
      logger.log(Level.SEVERE,
          "Error getting game totals for game", e);
    }
    logger.info("Game Total :" + gameTotal);
    return Optional.ofNullable(gameTotal);
  }



  /**
   * 
   * @param row
   * @return
   * @throws SQLException
   */
  private Player from(ResultSet row) throws SQLException {
    Player player = Player.newPlayer()
        .pk(row.getLong("id"))
        .playerId(row.getString("player_id"))
        .username(row.getString("player_name"))
        .right(row.getInt("guess_right"))
        .wrong(row.getInt("guess_wrong"))
        .score(row.getInt("guess_score"))
        .creationServer(row.getString("creation_server"))
        .scoringServer(row.getString("scoring_server"))
        .gameServer(row.getString("game_server"))
        .avatar(jsonb.fromJson(row.getString("player_avatar"), Avatar.class))
        .gameId(row.getString("game_id"));
    return player;
  }

  /**
   * 
   * @param row
   * @return
   */
  private GameTotal gameTotal(ResultSet row) throws SQLException {
    GameTotal gameTotal = GameTotal.newGameTotal()
        .totalPlayers(row.getLong("total_players"))
        .totalDollars(row.getLong("total_dollars"))
        .totalRights(row.getLong("total_rights"))
        .totalWrongs(row.getLong("total_wrongs"));
    return gameTotal;
  }

  /**
   * 
   * @param rs
   * @return
   * @throws SQLException
   */
  private List<Player> playersList(ResultSet rs) throws SQLException {
    List<Player> listOfPlayers = new ArrayList<>();
    while (rs.next()) {
      listOfPlayers.add(from(rs));
    }
    return listOfPlayers;
  }
}
