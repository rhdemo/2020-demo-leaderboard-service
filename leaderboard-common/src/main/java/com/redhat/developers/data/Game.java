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

import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Game
 */
@RegisterForReflection
@JsonIgnoreProperties({"date", "configuration"})
public class Game {
  private long id;
  private String gameId;
  private String state;
  private OffsetDateTime date;
  private String configuration;

  public Game() {

  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getGameId() {
    return gameId;
  }

  public void setGameId(String gameId) {
    this.gameId = gameId;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public OffsetDateTime getDate() {
    return date;
  }

  public void setDate(OffsetDateTime date) {
    this.date = date;
  }

  public String getConfiguration() {
    return configuration;
  }

  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  public static Game newGame() {
    return new Game();
  }

  public Game id(long id) {
    this.id = id;
    return this;
  }

  public Game gameId(String gameId) {
    this.gameId = gameId;
    return this;
  }

  public Game state(String state) {
    this.state = state;
    return this;
  }

  public Game date(OffsetDateTime date) {
    this.date = date;
    return this;
  }

  public Game configuration(String configuration) {
    this.configuration = configuration;
    return this;
  }
}
