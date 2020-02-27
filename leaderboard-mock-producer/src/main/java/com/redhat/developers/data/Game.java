package com.redhat.developers.data;

/**
 * Game
 */
public class Game {
  public String id;
  public String state;
  public String date;

  public Game(String id, String state, String date) {
    this.id = id;
    this.state = state;
    this.date = date;
  }

}