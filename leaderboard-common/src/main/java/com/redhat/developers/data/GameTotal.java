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
  private long totalRights;
  private long totalWrongs;
  private long totalDollars;

  public long getTotalPlayers() {
    return totalPlayers;
  }

  public void setTotalPlayers(long totalPlayers) {
    this.totalPlayers = totalPlayers;
  }

  public long getTotalRights() {
    return totalRights;
  }

  public void setTotalRights(long totalRights) {
    this.totalRights = totalRights;
  }

  public long getTotalWrongs() {
    return totalWrongs;
  }

  public void setTotalWrongs(long totalWrongs) {
    this.totalWrongs = totalWrongs;
  }

  public long getTotalDollars() {
    return totalDollars;
  }

  public void setTotalDollars(long totalDollars) {
    this.totalDollars = totalDollars;
  }

  public static GameTotal newGameTotal() {
    return new GameTotal();
  }

  public GameTotal totalPlayers(long totalPlayers) {
    setTotalPlayers(totalPlayers);
    return this;
  }

  public GameTotal totalDollars(long totalDollars) {
    setTotalDollars(totalDollars);
    return this;
  }

  public GameTotal totalRights(long totalRights) {
    setTotalRights(totalRights);
    return this;
  }

  public GameTotal totalWrongs(long totalWrongs) {
    setTotalWrongs(totalWrongs);
    return this;
  }
}
