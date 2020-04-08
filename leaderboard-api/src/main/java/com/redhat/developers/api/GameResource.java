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
import java.util.Optional;
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
import javax.ws.rs.core.Response.Status;
import com.redhat.developers.data.Game;
import com.redhat.developers.sql.GameQueries;
import io.smallrye.mutiny.Uni;

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

  @Path("/game/all")
  @GET
  public Uni<Response> all() {
    logger.info("Finding active game");
    List<Game> games = gameQueries.findAll();
    return Uni.createFrom().item(Response.ok().entity(games).build());
  }

  @Path("/game/active")
  @GET
  public Uni<Response> activeGame() {
    logger.info("Finding active game");
    Optional<Game> game = gameQueries.findActiveGame();

    if (game.isPresent()) {
      return Uni.createFrom().item(Response.ok().entity(game).build());
    }
    return Uni.createFrom().item(Response.noContent().build());
  }

  @GET
  @Path("/game/{id}")
  public Uni<Response> find(@PathParam("id") Integer id) {
    logger.log(Level.FINE, "Finding game by id {0} ", id);
    Optional<Game> game = gameQueries.findById(id);
    if (game.isPresent()) {
      return Uni.createFrom().item(Response.ok().entity(game).build());
    }
    return Uni.createFrom().item(Response.status(Status.NOT_FOUND).build());
  }

  @POST
  @Path("/game/save")
  public Uni<Response> save(Game game) {
    logger.log(Level.FINE, "Saving game {0} ", game.getPk());
    boolean saved = gameQueries.upsert(game);
    if (saved) {
      return Uni.createFrom().item(Response.accepted().build());
    }
    return Uni.createFrom().item(Response.noContent().build());
  }

  @DELETE
  @Path("/game/{id}")
  public Uni<Response> delete(@PathParam("id") Integer id) {
    logger.log(Level.FINE, "Deleting game with id {0} ", id);
    boolean deleted = gameQueries.delete(id);
    if (deleted) {
      return Uni.createFrom().item(Response.noContent().build());
    }
    return Uni.createFrom().item(Response.status(Status.NOT_FOUND).build());
  }

}
