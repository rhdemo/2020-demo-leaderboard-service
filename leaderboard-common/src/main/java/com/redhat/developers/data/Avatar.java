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

import javax.enterprise.inject.Produces;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Avatar
 */
@RegisterForReflection
public class Avatar {
  public int body;
  public int eyes;
  public int ears;
  public int mouth;
  public int nose;
  public int color;

  public Avatar() {}

  @Produces
  public static Avatar newAvatar() {
    return new Avatar();
  }

  public int getBody() {
    return body;
  }

  public Avatar body(int body) {
    this.body = body;
    return this;
  }

  public int getEyes() {
    return eyes;
  }

  public Avatar eyes(int eyes) {
    this.eyes = eyes;
    return this;
  }

  public int getEars() {
    return ears;
  }

  public Avatar ears(int ears) {
    this.ears = ears;
    return this;
  }

  public int getMouth() {
    return mouth;
  }

  public Avatar mouth(int mouth) {
    this.mouth = mouth;
    return this;
  }

  public int getNose() {
    return nose;
  }

  public Avatar nose(int nose) {
    this.nose = nose;
    return this;
  }

  public int getColor() {
    return color;
  }

  public Avatar color(int color) {
    this.color = color;
    return this;
  }

}
