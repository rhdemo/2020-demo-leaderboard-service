package com.redhat.developers.data;

import javax.enterprise.inject.Produces;

/**
 * Player
 */
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

  @Produces
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
    this.score += score;
    return this;
  }

  /**
   * @deprecated
   * @param isCorrect
   * @param score
   * @return
   */
  public Player score(boolean isCorrect, int score) {
    return score(score);
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

}
