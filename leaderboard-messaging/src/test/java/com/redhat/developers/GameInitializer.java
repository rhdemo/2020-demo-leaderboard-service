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
package com.redhat.developers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import com.redhat.developers.data.Game;
import com.redhat.developers.data.GameMessage;
import com.redhat.developers.data.GameState;
import com.redhat.developers.data.Player;
import com.redhat.developers.sql.GameQueries;
import com.redhat.developers.sql.PlayerQueries;

/**
 * GameInitService
 */
@ApplicationScoped
public class GameInitializer {

  Logger logger = Logger.getLogger("GameInitializer");

  @Inject
  Jsonb jsonb;

  @Inject
  GameQueries gameQueries;

  @Inject
  PlayerQueries playerQueries;

  Game game;

  List<GameMessage> gameMessages;

  @PostConstruct
  void init() throws Exception {
    this.game = Game.newGame()
        .gameId("new-game-1583157438")
        .state(GameState.byCode(1))
        .configuration("{}");

    long pk = gameQueries.upsert(game);
    assertTrue(pk > 0);
    this.game = this.game.pk(pk);
  }

  public Optional<Game> gameExist() {
    List<Game> games = gameQueries
        .findAll();
    this.game = games.get(0);
    return Optional.ofNullable(game);
  }

  public void deleteGame() {
    gameQueries.delete(this.game.getPk());
  }

  public List<Player> seedPlayers()
      throws Exception {
    URL dataFileUrl = this.getClass().getResource("/data.json");
    Jsonb jsonb = JsonbBuilder.newBuilder().build();
    List<GameMessage> gameMessages = jsonb.fromJson(dataFileUrl.openStream(),
        new ArrayList<GameMessage>() {}.getClass()
            .getGenericSuperclass());
    return gameMessages.stream()
        .map(g -> g.getPlayer())
        .collect(Collectors.toList());
  }

}
