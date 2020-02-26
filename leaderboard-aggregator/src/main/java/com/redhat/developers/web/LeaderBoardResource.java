package com.redhat.developers.web;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.redhat.developers.service.LeaderBoardService;

/**
 * LeaderBoardSource
 * TODO: Auth
 */

@ApplicationScoped
@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LeaderBoardResource {
  //TODO WebSocket

  @Inject
  LeaderBoardService service;

  @GET
  @Path("leaderboard")
  public Response getLeaderBoard() {
     return Response.ok(service.leaderBoardWithRankings()).build();
   }
}