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
import javax.enterprise.inject.Produces;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Game
 */
@RegisterForReflection
@JsonIgnoreProperties({"date", "configuration"})
public class Game {
  public String id;
  public GameState state;
  public OffsetDateTime date;
  public String configuration;

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

  public String getConfiguration() {
    return configuration;
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

  public Game configuration(String configuration) {
    this.configuration = configuration;
    return this;
  }

}
