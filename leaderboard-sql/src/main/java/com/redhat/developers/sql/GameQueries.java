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
import com.redhat.developers.data.Game;
import com.redhat.developers.data.GameState;
import io.agroal.api.AgroalDataSource;

/**
 * GameQueries
 */
@ApplicationScoped
public class GameQueries {

  static Logger logger = Logger.getLogger(GameQueries.class.getName());

  @Inject
  AgroalDataSource dataSource;


  public List<Game> findAll() {
    logger.info("Find All");
    List<Game> games = new ArrayList<>();
    try (Connection dbConn = dataSource.getConnection()) {
      PreparedStatement pst = dbConn
          .prepareStatement("SELECT * from games ORDER BY game_date DESC");
      ResultSet resultSet = pst.executeQuery();
      games = games(resultSet);
      pst.close();
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error retriving all games ",
          e);
    }
    return games;
  }

  public Optional<Game> findById(int id) {
    logger.info("Finding game with id " + id);
    Game game = null;
    try (Connection dbConn = dataSource.getConnection()) {
      PreparedStatement pst = dbConn.prepareStatement("SELECT * from games "
          + " WHERE  id=?"
          + "ORDER BY game_date DESC");
      pst.setLong(1, id);
      ResultSet rs = pst.executeQuery();
      if (rs.next()) {
        game = from(rs);
      }
      pst.close();
    } catch (SQLException e) {
      logger.log(Level.SEVERE,
          "Error Finding game with id " + id, e);
    }
    return Optional.ofNullable(game);
  }

  public Optional<Game> findActiveGame() {
    logger.info("Finding Acive game");
    Game game = null;
    try (Connection dbConn = dataSource.getConnection()) {
      PreparedStatement pst = dbConn.prepareStatement("SELECT * from games "
          + " WHERE game_state=1"
          + " ORDER BY game_date DESC"
          + " FETCH FIRST 1 ROW ONLY");
      ResultSet rs = pst.executeQuery();
      if (rs.next()) {
        game = from(rs);
      }
      pst.close();
    } catch (SQLException e) {
      logger.log(Level.SEVERE,
          "Error Finding active game ", e);
    }
    return Optional.ofNullable(game);
  }

  public Boolean delete(long id) {
    logger.info("Deleting game with id " + id);
    boolean isDeleted = false;
    try (Connection dbConn = dataSource.getConnection()) {
      PreparedStatement pst = dbConn
          .prepareStatement("DELETE FROM games WHERE id=?");
      pst.setLong(1, id);
      int rowCount = pst.executeUpdate();
      isDeleted = rowCount == 1;
      pst.close();
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error deleting game with id " + id,
          e);
    }
    return isDeleted;
  }

  /**
   * 
   * @param game
   * @return
   */
  public Boolean upsert(Game game) {
    logger.info("Upserting game with id " + game.getPk());
    boolean isSaved = false;
    try (Connection dbConn = dataSource.getConnection()) {
      PreparedStatement pst = dbConn
          .prepareStatement("INSERT INTO games(game_id,game_config,game_state)"
              + " VALUES (?,?::JSONB,?)"
              + " ON CONFLICT (game_id)"
              + " DO"
              + "   UPDATE set "
              + "     game_config=?::JSONB,"
              + "     game_state=?");
      gameTuple(pst, game);
      int rowCount = pst.executeUpdate();
      isSaved = rowCount == 1;
      pst.close();
    } catch (SQLException e) {
      logger.log(Level.SEVERE,
          "Error Upserting Game with id " + game.getPk(), e);
    }
    return isSaved;
  }

  /**
   * 
   * @param row
   * @return
   * @throws SQLException
   */
  private Game from(ResultSet row) throws SQLException {

    return Game.newGame()
        .pk(row.getLong("id"))
        .gameId(row.getString("game_id"))
        .configuration(row.getString("game_config"))
        .date(row.getString("game_date"))
        .state(GameState.byCode(row.getInt("game_state")));
  }

  /**
   * 
   * @param resultSet
   * @return
   * @throws SQLException
   */
  private List<Game> games(ResultSet resultSet) throws SQLException {
    List<Game> listOfGames = new ArrayList<>();
    while (resultSet.next()) {
      listOfGames.add(from(resultSet));
    }
    return listOfGames;
  }

  /**
   * 
   * @param game
   * @return
   */
  private void gameTuple(PreparedStatement pst, Game game) throws SQLException {
    pst.setString(1, game.getGameId());// Param Order 1
    pst.setString(2, game.getConfiguration()); // Param Order 2
    pst.setInt(3, GameState.byCodeString(game.getState())); // Param Order 3

    // TODO update better
    pst.setString(4, game.getConfiguration()); // Param Order 4
    pst.setInt(5, GameState.byCodeString(game.getState())); // Param Order 5
  }
}
