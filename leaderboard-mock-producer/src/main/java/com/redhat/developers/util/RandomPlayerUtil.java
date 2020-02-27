package com.redhat.developers.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;

import com.redhat.developers.data.Game;
import com.redhat.developers.data.Player;
import com.redhat.developers.data.ScoringKafkaMessage;

import org.apache.commons.lang3.RandomUtils;

/**
 * RandomPlayerUtil
 */
@ApplicationScoped
public class RandomPlayerUtil {

  private final List<String> playerNames = Arrays.asList("tom", "jerry", "donald", "mickey", "pluto", "goofy");

  @Inject
  Jsonb jsonb;

  public String generate(String source) {
    Game game = newGame();
    Collections.rotate(playerNames, 1);
    String playerName = playerNames.get(0);
    int rights = RandomUtils.nextInt(0, 10);
    int wrongs = RandomUtils.nextInt(5, 10);
    int totalScore = RandomUtils.nextInt(10, 100);
    Player player = new Player(playerName, playerName, rights, wrongs, totalScore, source, avatar(),game);

    ScoringKafkaMessage scoringMessage = new ScoringKafkaMessage();
    scoringMessage.setGame(game);
    scoringMessage.setPlayer(player);
    return jsonb.toJson(scoringMessage);
  }

  private Game newGame() {
    return new Game("new-game-1582548335", "active", "2020-02-24T12:45:35.000Z");
  }

  private String avatar() {
    Avatar avatar = new Avatar();
    avatar.body = RandomUtils.nextInt(0, 5);
    avatar.color = RandomUtils.nextInt(3, 5);
    avatar.ears = RandomUtils.nextInt(0, 3);
    avatar.eyes = RandomUtils.nextInt(0, 5);
    avatar.mouth = RandomUtils.nextInt(1, 3);
    avatar.nose = RandomUtils.nextInt(2, 4);
    return jsonb.toJson(avatar);
  }

  public static final class Avatar {
    public int body;
    public int eyes;
    public int mouth;
    public int ears;
    public int nose;
    public int color;
  }
}