package com.redhat.developers.data;

/**
 * ScoringKafkaMessage
 */
public class ScoringKafkaMessage {

  private Player player;
  private Game game;

  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public Game getGame() {
    return game;
  }

  public void setGame(Game game) {
    this.game = game;
  }

}