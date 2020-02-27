package com.redhat.developers.data;

/**
 * Player
 */
public class Player {
  public String playerId;
  public String playerName;
  public int right;
  public int wrong;
  public int score;
  public String clusterSource;
  public String avatar;
  public Game game;

  public Player(String playerId, String playerName, int right, int wrong, int score, String clusterSource,
      String avatar, Game game) {
    this.playerId = playerId;
    this.playerName = playerName;
    this.right = right;
    this.wrong = wrong;
    this.score = score;
    this.clusterSource = clusterSource;
    this.avatar = avatar;
    this.game = game;
  }

}