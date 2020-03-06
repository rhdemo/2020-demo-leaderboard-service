package com.redhat.developers.web;

import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import com.redhat.developers.service.PlayerPersistenceService;

/**
 * LeaderBoardSource TODO: Auth, Exception
 */
@ApplicationScoped
@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LeaderBoardResource {

  private Logger logger = Logger.getLogger(LeaderBoardResource.class.getName());

  @Inject
  PlayerPersistenceService playerPService;

  @GET
  @Path("leaderboard/{gameid}")
  public CompletionStage<Response> getLeaderBoard(
      @PathParam("gameid") String gameId) {
    logger.info("Getting Ranked players for game " + gameId);
    return playerPService.rankedPlayerList(gameId)
        .thenApply(results -> Response.ok(results))
        .exceptionally(e -> {
          logger.log(Level.SEVERE, "Error while getting players with ranks", e);
          return Response.status(Status.INTERNAL_SERVER_ERROR);
        })
        .thenApply(ResponseBuilder::build);
  }
}
