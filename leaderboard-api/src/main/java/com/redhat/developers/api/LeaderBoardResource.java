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

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import com.redhat.developers.data.Player;
import com.redhat.developers.sql.PlayerQueries;
import io.vertx.axle.pgclient.PgPool;

/**
 * LeaderBoardSource TODO: Auth, Exception
 */
@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LeaderBoardResource {

  private Logger logger = Logger.getLogger(LeaderBoardResource.class.getName());

  @Inject
  PlayerQueries playerQueries;

  @Inject
  PgPool client;

  @GET
  @Path("leaderboard")
  public CompletionStage<Response> getLeaderBoard(
      @QueryParam("rowCount") String qRowCount) {
    logger.log(Level.FINE, "Getting Ranked {0} player(s) for game ", qRowCount);
    int rowCount = qRowCount != null ? Integer.parseInt(qRowCount) : 10;
    return rankedPlayerList(rowCount)
        .thenApply(results -> Response.ok(results))
        .exceptionally(e -> {
          logger.log(Level.SEVERE, "Error while getting players with ranks", e);
          return Response.status(Status.INTERNAL_SERVER_ERROR);
        })
        .thenApply(ResponseBuilder::build);
  }

  /**
   * 
   * @param gameId
   * @return
   */
  private CompletionStage<List<Player>> rankedPlayerList(int rowCount) {
    return playerQueries.rankPlayers(client, rowCount);
  }

}
