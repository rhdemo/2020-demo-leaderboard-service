package com.redhat.developers.api;

import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import com.redhat.developers.data.Game;
import com.redhat.developers.sql.GameQueries;
import io.vertx.axle.pgclient.PgPool;

/**
 * GameResource
 */
@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GameResource {

  private Logger logger = Logger.getLogger(GameResource.class.getName());

  @Inject
  GameQueries gameQueries;

  @Inject
  PgPool client;

  @Path("/game/active")
  @GET
  public CompletionStage<Response> activeGame() {
    logger.info("Finding active game");
    return gameQueries.findActiveGame(client)
        .thenApply(oPlayer -> oPlayer.isPresent() ? Response.ok(oPlayer.get())
            : Response.status(Status.NOT_FOUND))
        .thenApply(ResponseBuilder::build);
  }

  @Path("/game/state/{state}")
  @GET
  public CompletionStage<Response> gamesByState(
      @PathParam("state") String state) {
    logger.log(Level.FINE, "Finding games by state {0} ", state);
    return gameQueries.gamesByState(client, state)
        .thenApply(games -> Response.ok(games))
        .thenApply(ResponseBuilder::build);
  }

  @GET
  @Path("/game/{id}")
  public CompletionStage<Response> find(@PathParam("id") String gameId) {
    logger.log(Level.FINE, "Finding game by id {0} ", gameId);
    return gameQueries.findById(client, gameId)
        .thenApply(oPlayer -> oPlayer.isPresent() ? Response.ok(oPlayer.get())
            : Response.status(Status.NOT_FOUND))
        .thenApply(ResponseBuilder::build);
  }

  @POST
  @Path("/game/save")
  public CompletionStage<Response> add(Game game) {
    logger.log(Level.FINE, "Saving game {0} ", game.getId());
    return gameQueries.upsert(client, game)
        .thenApply(b -> b ? Response.status(Status.ACCEPTED)
            : Response.status(Status.INTERNAL_SERVER_ERROR))
        .thenApply(ResponseBuilder::build);
  }

  @DELETE
  @Path("/game/{id}")
  public CompletionStage<Response> delete(@PathParam("id") String gameId) {
    logger.log(Level.FINE, "Deleting game with id {0} ", gameId);
    return gameQueries.delete(client, gameId)
        .thenApply(b -> b ? Response.status(Status.NO_CONTENT)
            : Response.status(Status.NOT_FOUND))
        .thenApply(ResponseBuilder::build);
  }


}
