/*-
 * #%L
 * Leaderboard Aggregator Common
 * %%
 * Copyright (C) 2020 Red Hat Inc.,
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.redhat.developers.data;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Player
 */
@RegisterForReflection
public class Player {

  private long id;
  private String playerId;
  private String username;
  private int right;
  private int wrong;
  private int score;
  private String creationServer;
  private String gameServer;
  private String scoringServer;
  private Avatar avatar;
  private String gameId;

  public Player() {

  }

  public static Player newPlayer() {
    return new Player();
  }

  public Player id(long id) {
    this.id = id;
    return this;
  }

  public Player playerId(String playerId) {
    this.playerId = playerId;
    return this;
  }

  public Player username(String username) {
    this.username = username;
    return this;
  }

  public Player right(int right) {
    this.right = right;
    return this;
  }

  public Player wrong(int wrong) {
    this.wrong = wrong;
    return this;
  }

  public Player score(int score) {
    this.score = score;
    return this;
  }

  public Player creationServer(String creationServer) {
    this.creationServer = creationServer;
    return this;
  }

  public Player gameServer(String gameServer) {
    this.gameServer = gameServer;
    return this;
  }

  public Player scoringServer(String scoringServer) {
    this.scoringServer = scoringServer;
    return this;
  }

  public Player avatar(Avatar avatar) {
    this.avatar = avatar;
    return this;
  }

  public Player gameId(String gameId) {
    this.gameId = gameId;
    return this;
  }

  public long getId() {
    return id;
  }


  public void setId(long id) {
    this.id = id;
  }


  public String getPlayerId() {
    return playerId;
  }


  public void setPlayerId(String playerId) {
    this.playerId = playerId;
  }


  public String getUsername() {
    return username;
  }


  public void setUsername(String username) {
    this.username = username;
  }


  public int getRight() {
    return right;
  }


  public void setRight(int right) {
    this.right = right;
  }


  public int getWrong() {
    return wrong;
  }


  public void setWrong(int wrong) {
    this.wrong = wrong;
  }


  public int getScore() {
    return score;
  }


  public void setScore(int score) {
    this.score = score;
  }


  public String getCreationServer() {
    return creationServer;
  }


  public void setCreationServer(String creationServer) {
    this.creationServer = creationServer;
  }


  public String getGameServer() {
    return gameServer;
  }


  public void setGameServer(String gameServer) {
    this.gameServer = gameServer;
  }


  public String getScoringServer() {
    return scoringServer;
  }


  public void setScoringServer(String scoringServer) {
    this.scoringServer = scoringServer;
  }


  public Avatar getAvatar() {
    return avatar;
  }


  public void setAvatar(Avatar avatar) {
    this.avatar = avatar;
  }


  public String getGameId() {
    return gameId;
  }


  public void setGameId(String gameId) {
    this.gameId = gameId;
  }
}
