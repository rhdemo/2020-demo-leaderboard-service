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

public class GameState {

  public static int byCodeString(String state) {
    switch (state) {
      case "invalid":
        return -1;
      case "active":
        return 1;
      case "lobby":
        return 2;
      case "bonus":
        return 3;
      case "paused":
        return 4;
      case "stopped":
        return 5;
      default:
        return -1;
    }
  }

  public static String byCode(int code) {
    switch (code) {
      case -1:
        return "invalid";
      case 0:
        return "loading";
      case 1:
        return "active";
      case 2:
        return "lobby";
      case 3:
        return "bonus";
      case 4:
        return "paused";
      default:
        return "invalid";
    }
  }

}
