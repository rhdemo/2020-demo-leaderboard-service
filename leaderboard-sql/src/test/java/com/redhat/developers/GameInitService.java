package com.redhat.developers;

import java.net.URL;
import java.time.Duration;
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
import io.vertx.mutiny.pgclient.PgPool;

/**
 * GameInitService
 */
@ApplicationScoped
public class GameInitService {

  @Inject
  GameQueries gameQueries;

  @Inject
  PgPool client;

  Game game = Game.newGame()
      .gameId("new-game-1583157438")
      .state(GameState.byCode(1))
      .configuration("{}");

  @PostConstruct
  void init() {
    gameQueries
        .upsert(client, game)
        .await().atMost(Duration.ofSeconds(10));
  }

  public Optional<Game> gameExist() {
    Optional<List<Game>> games = gameQueries
        .findAll(client)
        .await().asOptional().atMost(Duration.ofSeconds(10));
    return Optional.ofNullable(games.get().get(0));
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
