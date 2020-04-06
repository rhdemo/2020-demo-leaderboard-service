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

@RegisterForReflection
public class GameTotal {

  private long totalPlayers;
  private long totalGuesses;
  private long totalDollars;

  public long getTotalPlayers() {
    return totalPlayers;
  }

  public long getTotalDollars() {
    return totalDollars;
  }

  public void setTotalDollars(long totalDollars) {
    this.totalDollars = totalDollars;
  }

  public long getTotalGuesses() {
    return totalGuesses;
  }

  public void setTotalGuesses(long totalGuesses) {
    this.totalGuesses = totalGuesses;
  }

  public void setTotalPlayers(long totalPlayers) {
    this.totalPlayers = totalPlayers;
  }

  public static GameTotal newGameTotal() {
    return new GameTotal();
  }

  public GameTotal totalPlayers(long totalPlayers) {
    this.totalPlayers = totalPlayers;
    return this;
  }

  public GameTotal totalGuesses(long totalGuesses) {
    this.totalGuesses = totalGuesses;
    return this;
  }

  public GameTotal totalDollars(long totalDollars) {
    this.totalDollars = totalDollars;
    return this;
  }

}
