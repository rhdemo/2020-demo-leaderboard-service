package com.redhat.developers.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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
        .gameId(newGame().getId());

    GameMessage scoringMessage = new GameMessage();
    scoringMessage.setGame(game);
    scoringMessage.setPlayer(player);
    return scoringMessage;
  }

  private Game newGame() {
    return Game.newGame()
        .id(GAME_ID)
        .state("active")
        .date("2020-02-24T12:45:35.000Z");
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

  public List<GameMessage> fromFile(InputStream fin)
      throws Exception {
    Jsonb jsonb = JsonbBuilder.newBuilder().build();
    return jsonb.fromJson(fin,
        new ArrayList<GameMessage>() {}.getClass()
            .getGenericSuperclass());
  }

  public static void main(String[] args) {
    Scorer scorer = new Scorer();
    List<GameMessage> messages = new ArrayList<>();
    // Flowable.range(1, 100)
    // .forEach(tick -> {
    // messages.add(scorer.generate());
    // });
    Jsonb jsonb = JsonbBuilder.newBuilder().build();
    // System.out.println(jsonb.toJson(messages));
    try {
      FileInputStream fin = new FileInputStream(
          "/Users/kameshs/git/rhdemo/2020-demo-leaderboard-service/leaderboard-aggregator/src/test/resources/data.json");
      messages = scorer.fromFile(fin);
      System.out.println(messages.size());
      List<GameMessage> sgMessages = messages.stream()
          .filter(skm -> {
            Player player = skm.getPlayer();
            // System.out.println(jsonb.toJson(player));
            return player.getCreationServer().equals("sg");
          })
          .collect(Collectors.toList());
      System.out.println(sgMessages.size());
      sgMessages.forEach(
          s -> System.out.println(jsonb.toJson(s.getPlayer(), Player.class)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
