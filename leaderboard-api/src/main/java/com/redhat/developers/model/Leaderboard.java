package com.redhat.developers.model;

import java.util.List;
import javax.json.bind.annotation.JsonbProperty;
import com.redhat.developers.data.Player;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Leaderboard {

  private List<Player> leaders;
  @JsonbProperty("total_players")
  private long players;
  @JsonbProperty("total_rights")
  private long rights;
  @JsonbProperty("total_wrongs")
  private long wrongs;
  @JsonbProperty("total_dollars")
  private long dollars;

  public List<Player> getLeaders() {
    return leaders;
  }

  public void setLeaders(List<Player> leaders) {
    this.leaders = leaders;
  }

  public long getPlayers() {
    return players;
  }

  public void setPlayers(long players) {
    this.players = players;
  }

  public long getRights() {
    return rights;
  }

  public void setRights(long rights) {
    this.rights = rights;
  }

  public long getWrongs() {
    return wrongs;
  }

  public void setWrongs(long wrongs) {
    this.wrongs = wrongs;
  }

  public long getDollars() {
    return dollars;
  }

  public void setDollars(long dollars) {
    this.dollars = dollars;
  }

}
