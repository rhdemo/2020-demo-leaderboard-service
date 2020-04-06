/*-
 * #%L
 * Leaderboard API
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
package com.redhat.developers.api;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.redhat.developers.data.GameTotal;
import com.redhat.developers.data.Player;
import com.redhat.developers.model.Leaderboard;
import com.redhat.developers.sql.PlayerQueries;
import io.smallrye.mutiny.Uni;

/**
 * LeaderBoardSource TODO: Auth
 */
@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LeaderBoardResource {

  private Logger logger = Logger.getLogger(LeaderBoardResource.class.getName());

  @Inject
  PlayerQueries playerQueries;

  @Inject
  @Named("gamedb")
  Connection dbConn;

  @GET
  @Path("leaderboard")
  public Uni<Response> getLeaderBoard(
      @QueryParam("rowCount") String qRowCount) {
    logger.log(Level.FINE, "Getting Ranked {0} player(s) for game ", qRowCount);
    int rowCount = qRowCount != null ? Integer.parseInt(qRowCount) : 10;
    List<Player> leaders = playerQueries.rankPlayers(dbConn, rowCount);
    Optional<GameTotal> gameTotals = playerQueries.gameTotals(dbConn);
    Leaderboard leaderboard = new Leaderboard();
    leaderboard.setLeaders(leaders);
    if (gameTotals.isPresent()) {
      GameTotal gameTotal = gameTotals.get();
      leaderboard.setDollars(gameTotal.getTotalDollars());
      leaderboard.setGuesses(gameTotal.getTotalGuesses());
      leaderboard.setPlayers(gameTotal.getTotalPlayers());
    }
    return Uni.createFrom().item(Response.ok().entity(leaderboard).build());
  }

}
