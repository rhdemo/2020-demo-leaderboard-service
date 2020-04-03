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
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;

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

  @Path("/game/all")
  @GET
  public Uni<Response> all() {
    logger.info("Finding active game");
    return gameQueries.findAll(client)
        .map(players -> Response.ok(players))
        .map(ResponseBuilder::build);
  }

  @Path("/game/active")
  @GET
  public Uni<Response> activeGame() {
    logger.info("Finding active game");
    return gameQueries.findActiveGame(client)
        .map(oPlayer -> oPlayer.isPresent() ? Response.ok(oPlayer.get())
            : Response.status(Status.NOT_FOUND))
        .map(ResponseBuilder::build);
  }

  @GET
  @Path("/game/{id}")
  public Uni<Response> find(@PathParam("id") Integer id) {
    logger.log(Level.FINE, "Finding game by id {0} ", id);
    return gameQueries.findById(client, id)
        .map(oPlayer -> oPlayer.isPresent() ? Response.ok(oPlayer.get())
            : Response.status(Status.NOT_FOUND))
        .map(ResponseBuilder::build);
  }

  @POST
  @Path("/game/save")
  public Uni<Response> save(Game game) {
    logger.log(Level.FINE, "Saving game {0} ", game.getPk());
    return gameQueries.upsert(client, game)
        .map(b -> b ? Response.accepted() : Response.noContent())
        .map(ResponseBuilder::build);
  }

  @DELETE
  @Path("/game/{id}")
  public Uni<Response> delete(@PathParam("id") Integer id) {
    logger.log(Level.FINE, "Deleting game with id {0} ", id);
    return gameQueries.delete(client, id)
        .map(b -> b ? Status.NO_CONTENT : Status.NOT_FOUND)
        .map(status -> Response.status(status).build());
  }

}
