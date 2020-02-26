package com.redhat.developers.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;

import com.redhat.developers.data.Player;

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
    Collections.rotate(playerNames, 1);
    String playerName = playerNames.get(0);
    int rights = RandomUtils.nextInt(0, 10);
    int wrongs = RandomUtils.nextInt(5, 10);
    int totalScore = RandomUtils.nextInt(1000, 2000);
    Player player = new Player(playerName, playerName, rights, wrongs, totalScore, source,"http://example.com/"+playerName);
    return jsonb.toJson(player);
  }
}