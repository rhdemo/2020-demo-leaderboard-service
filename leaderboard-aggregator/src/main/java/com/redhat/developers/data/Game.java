package com.redhat.developers.data;

import javax.enterprise.inject.Produces;

/**
 * Game
 */
public class Game {
  public String id;
  public String state;
  public String date;

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

  public String getState() {
    return state;
  }

  public Game state(String state) {
    this.state = state;
    return this;

  }

  public String getDate() {
    return date;
  }

  public Game date(String date) {
    this.date = date;
    return this;
  }

}
