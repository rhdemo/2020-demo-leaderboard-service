package com.redhat.developers.model;

import java.util.List;
import com.redhat.developers.data.Player;

public class Leaderboard {

  private List<Player> leaders;
  private long players;
  private long guesses;
  private long dollars;

  public List<Player> getLeaders() {
    return leaders;
  }

  public long getDollars() {
    return dollars;
  }

  public void setDollars(long dollars) {
    this.dollars = dollars;
  }

  public long getGuesses() {
    return guesses;
  }

  public void setGuesses(long guesses) {
    this.guesses = guesses;
  }

  public long getPlayers() {
    return players;
  }

  public void setPlayers(long players) {
    this.players = players;
  }

  public void setLeaders(List<Player> leaders) {
    this.leaders = leaders;
  }
}
