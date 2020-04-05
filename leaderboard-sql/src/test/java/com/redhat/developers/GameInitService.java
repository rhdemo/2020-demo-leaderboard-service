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
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import com.redhat.developers.data.Game;
import com.redhat.developers.data.GameMessage;
import com.redhat.developers.data.GameState;
import com.redhat.developers.data.Player;
import com.redhat.developers.sql.GameQueries;

/**
 * GameInitService
 */
@ApplicationScoped
public class GameInitService {

  @Inject
  ConnectionUtil connectionUtil;

  @Inject
  GameQueries gameQueries;
  Connection client;

  Game game = Game.newGame()
      .gameId("new-game-1583157438")
      .state(GameState.byCode(1))
      .configuration("{}");

  @PostConstruct
  void init() throws Exception {

    if (client == null) {
      this.client = connectionUtil.getConnection();
    }
    Boolean isInserted = gameQueries
        .upsert(client, game);
    assertTrue(isInserted);
  }

  public Optional<Game> gameExist() {
    List<Game> games = gameQueries
        .findAll(client);
    return Optional.ofNullable(games.get(0));
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
