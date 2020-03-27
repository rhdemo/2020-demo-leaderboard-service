/*-
 * #%L
 * Leaderboard Aggregator Test
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
package com.redhat.developers.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import com.redhat.developers.data.Avatar;
import com.redhat.developers.data.Game;
import com.redhat.developers.data.GameMessage;
import com.redhat.developers.data.Player;
import org.apache.commons.lang3.RandomUtils;

/**
 * Scorer
 */
@ApplicationScoped
public class Scorer {

  public static final String GAME_ID = "new-game-1582548335";

  // Some GMT time
  OffsetDateTime someGMTDateTime = OffsetDateTime.of(
      LocalDateTime.of(2020, Month.MARCH, 9, 18, 01, 00),
      ZoneOffset.ofHoursMinutes(0, 0));

  private static final List<String> PLAYER_NAMES =
      Arrays.asList("tom", "jerry", "donald", "mickey", "pluto", "goofy",
          "minnie", "winnie", "daisy", "tigger");

  private static final List<String> EDGES =
      Arrays.asList("sg", "nyc", "sfo", "sp", "lon");

  public GameMessage generate() {
    Game game = newGame();
    String source = EDGES.get(RandomUtils.nextInt(0, 2));
    String scoreSource = EDGES.get(RandomUtils.nextInt(2, 4));
    String gamingSource = EDGES.get(RandomUtils.nextInt(1, 4));
    Collections.rotate(PLAYER_NAMES, 1);
    String playerName = PLAYER_NAMES.get(0);
    int rights = RandomUtils.nextInt(0, 10);
    int wrongs = RandomUtils.nextInt(5, 10);
    int totalScore = RandomUtils.nextInt(10, 50);
    Player player = Player.newPlayer()
        .id(playerName)
        .username(playerName)
        .right(rights)
        .wrong(wrongs)
        .score(totalScore)
        .creationServer(source)
        .scoringServer(scoreSource)
        .gameServer(gamingSource)
        .gameId(playerName)
        .avatar(avatar())
        .gameId(newGame().getGameId());

    GameMessage scoringMessage = new GameMessage();
    scoringMessage.setGame(game);
    scoringMessage.setPlayer(player);
    return scoringMessage;
  }

  private Game newGame() {
    return Game.newGame()
        .gameId(GAME_ID)
        .state("active")
        .date(someGMTDateTime);
  }

  private Avatar avatar() {
    return Avatar.newAvatar()
        .body(RandomUtils.nextInt(0, 5))
        .color(RandomUtils.nextInt(3, 5))
        .ears(RandomUtils.nextInt(0, 3))
        .eyes(RandomUtils.nextInt(1, 3))
        .nose(RandomUtils.nextInt(2, 4))
        .mouth(RandomUtils.nextInt(0, 5));
  }

  public static List<GameMessage> fromFile(InputStream fin)
      throws Exception {
    Jsonb jsonb = JsonbBuilder.newBuilder().build();
    return jsonb.fromJson(fin,
        new ArrayList<GameMessage>() {}.getClass()
            .getGenericSuperclass());
  }

  public static void main(String[] args) {
    List<GameMessage> messages = new ArrayList<>();
    // Flowable.range(1, 100)
    // .forEach(tick -> {
    // messages.add(scorer.generate());
    // });
    Jsonb jsonb = JsonbBuilder.newBuilder().build();
    // System.out.println(jsonb.toJson(messages));
    try {
      URL dataFileUrl = Scorer.class.getResource("/data.json");
      messages = fromFile(dataFileUrl.openStream());
      System.out.println(messages.size());
      List<GameMessage> sgMessages = messages.stream()
          .filter(skm -> {
            Player player = skm.getPlayer();
            // System.out.println(jsonb.toJson(player));
            return player.getCreationServer().equals("sg");
          })
          .collect(Collectors.toList());
      // System.out.println(sgMessages.size());
      sgMessages.forEach(
          s -> System.out.println(jsonb.toJson(s.getPlayer(), Player.class)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
