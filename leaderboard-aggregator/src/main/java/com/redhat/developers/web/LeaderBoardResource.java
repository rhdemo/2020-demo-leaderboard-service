package com.redhat.developers.web;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.redhat.developers.service.PlayerPersistenceService;

/**
 * LeaderBoardSource
 * TODO: Auth
 */

@ApplicationScoped
@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LeaderBoardResource {

  @Inject
  PlayerPersistenceService playerPService;

  @GET
  @Path("leaderboard/{gameid}")
  public Response getLeaderBoard(@PathParam("gameid") String gameId) {
     return Response.ok(playerPService.rankedPlayerList(gameId)).build();
   }
}