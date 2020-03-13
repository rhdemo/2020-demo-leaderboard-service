package com.redhat.developers.api;

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
import com.redhat.developers.service.PlayerPersistenceService;

/**
 * LeaderBoardSource TODO: Auth, Exception
 */
@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LeaderBoardResource {

  private Logger logger = Logger.getLogger(LeaderBoardResource.class.getName());

  @Inject
  PlayerPersistenceService playerPService;

  @GET
  @Path("leaderboard")
  public CompletionStage<Response> getLeaderBoard(
      @QueryParam("rowCount") String qRowCount) {
    logger.info(Level.FINE, "Getting Ranked {0} players for game ", qRowCount);
    int rowCount = 10;
    if (qRowCount == null) {
      try {
        rowCount = Integer.parseInt(qRowCount);
      } catch (Exception e) {
        // Ignore it
      }
    }
    return playerPService.rankedPlayerList(rowCount)
        .thenApply(results -> Response.ok(results))
        .exceptionally(e -> {
          logger.log(Level.SEVERE, "Error while getting players with ranks", e);
          return Response.status(Status.INTERNAL_SERVER_ERROR);
        })
        .thenApply(ResponseBuilder::build);
  }

}
