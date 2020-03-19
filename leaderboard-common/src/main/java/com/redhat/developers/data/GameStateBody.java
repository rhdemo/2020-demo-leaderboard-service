package com.redhat.developers.data;

/**
 * Game
 */
public class GameStateBody {

  private Game game;
  private String type;

  public GameStateBody() {

  }

  public void setGame(Game game) {
    this.game = game;
  }

  public Game getGame() {
    return game;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
