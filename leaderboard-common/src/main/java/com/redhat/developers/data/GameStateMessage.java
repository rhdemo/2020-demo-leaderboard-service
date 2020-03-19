package com.redhat.developers.data;

import javax.enterprise.inject.Produces;

/**
 * Game
 */
public class GameStateMessage {
  public GameStateBody body;

  public GameStateMessage() {

  }

  @Produces
  public static GameStateMessage newGameStateMessage() {
    return new GameStateMessage();
  }

  public GameStateMessage body(GameStateBody body) {
    this.body = body;
    return this;
  }

  public GameStateBody getGameStateBody() {
    return body;
  }

}
