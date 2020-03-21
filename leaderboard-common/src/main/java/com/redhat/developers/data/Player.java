package com.redhat.developers.data;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Player
 */
@RegisterForReflection
public class Player {

  public String id;
  public String username;
  public int right;
  public int wrong;
  public int score;
  public String creationServer;
  public String gameServer;
  public String scoringServer;
  public Avatar avatar;
  public String gameId;

  public Player() {

  }

  public static Player newPlayer() {
    return new Player();
  }

  public String getId() {
    return id;
  }

  public Player id(String id) {
    this.id = id;
    return this;
  }

  public String getUsername() {
    return username;
  }

  public Player username(String username) {
    this.username = username;
    return this;
  }

  public int getRight() {
    return right;
  }

  public Player right(int right) {
    this.right = right;
    return this;
  }

  public int getWrong() {
    return wrong;
  }

  public Player wrong(int wrong) {
    this.wrong = wrong;
    return this;
  }

  public int getScore() {
    return score;
  }

  public Player score(int score) {
    this.score = score;
    return this;
  }

  public String getCreationServer() {
    return creationServer;
  }

  public Player creationServer(String creationServer) {
    this.creationServer = creationServer;
    return this;
  }

  public String getGameServer() {
    return gameServer;
  }

  public Player gameServer(String gameServer) {
    this.gameServer = gameServer;
    return this;
  }

  public String getScoringServer() {
    return scoringServer;
  }

  public Player scoringServer(String scoringServer) {
    this.scoringServer = scoringServer;
    return this;
  }

  public Avatar getAvatar() {
    return avatar;
  }

  public Player avatar(Avatar avatar) {
    this.avatar = avatar;
    return this;
  }

  public String getGameId() {
    return gameId;
  }

  public Player gameId(String gameId) {
    this.gameId = gameId;
    return this;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((gameId == null) ? 0 : gameId.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Player other = (Player) obj;
    if (gameId == null) {
      if (other.gameId != null)
        return false;
    } else if (!gameId.equals(other.gameId))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return String.format("Player [gameId=%s, id=%s, username=%s]", gameId, id,
        username);
  }

}
