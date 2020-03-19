package com.redhat.developers.data;

import javax.enterprise.inject.Produces;

/**
 * Game
 */
public class GameStateBody {

  public Game game;

  public GameStateBody() {

  }

  @Produces
  public static GameStateBody newBody() {
    return new GameStateBody();
  }

  public GameStateBody game(Game game) {
    this.game = game;
    return this;
  }

  public Game getGame() {
    return game;
  }
}
