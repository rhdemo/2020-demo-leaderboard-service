package com.redhat.developers.data;

import java.time.OffsetDateTime;
import javax.enterprise.inject.Produces;

/**
 * Game
 */
public class Game {
  public String id;
  public GameState state;
  public OffsetDateTime date;
  public String config;

  public Game() {

  }

  @Produces
  public static Game newGame() {
    return new Game();
  }

  public String getId() {
    return id;
  }

  public Game id(String id) {
    this.id = id;
    return this;
  }

  public String getConfig() {
    return config;
  }

  public GameState getState() {
    return state;
  }

  public Game state(GameState state) {
    this.state = state;
    return this;
  }

  public Game state(String state) {
    this.state(GameState.valueOf(state));
    return this;
  }

  public OffsetDateTime getDate() {
    return date;
  }

  public Game date(OffsetDateTime date) {
    this.date = date;
    return this;
  }

  public Game date(String date) {
    // just ignore string dates
    return this;
  }

  public Game config(String config) {
    this.config = config;
    return this;
  }

}
