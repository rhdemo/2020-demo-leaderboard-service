package com.redhat.developers.data;

/**
 * Game
 */
public class GameStateMessage {
  private GameStateBody body;

  public GameStateMessage() {

  }

  public void setBody(GameStateBody body) {
    this.body = body;
  }

  public GameStateBody getBody() {
    return body;
  }

}
