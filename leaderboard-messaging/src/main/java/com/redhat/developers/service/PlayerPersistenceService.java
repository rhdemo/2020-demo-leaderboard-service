/*-
 * #%L
 * Leaderboard Reactive Messaging
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
package com.redhat.developers.service;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Player;
import com.redhat.developers.sql.PlayerQueries;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.kafka.client.serialization.JsonbSerde;

/**
 * PlayerPersistenceService
 */
@ApplicationScoped
public class PlayerPersistenceService {

  Logger logger = Logger.getLogger(PlayerPersistenceService.class.getName());

  @Inject
  Jsonb jsonb;

  @Inject
  PlayerQueries playerQueries;

  @ConfigProperty(name = "quarkus.kafka-streams.topics")
  String streamTopic;

  @Produces
  public Topology handleTransactions() {
    JsonbSerde<Player> playerSerde = new JsonbSerde<>(Player.class);
    StreamsBuilder builder = new StreamsBuilder();

    builder
        .stream(streamTopic,
            (Consumed.with(Serdes.String(), playerSerde)))
        .foreach((k, v) -> save(v));
    Topology topology = builder.build();
    logger.log(Level.FINE, topology.describe().toString());
    return topology;
  }

  protected void save(Player player) {
    final Instant startTime = Instant.now();
    logger.log(Level.FINE,
        "Saving Player  {0} ", player.getPlayerId());
    long pk = playerQueries.upsert(player);
    final Instant endTime = Instant.now();
    if (pk > 0) {
      logger.log(Level.FINE,
          "Player {0} Saved with Primary key {1} in {2} ms",
          new Object[] {player.getPlayerId(), pk,
              Duration.between(startTime, endTime).toMillis()});
    } else {
      logger.log(Level.FINE,
          "Unable to save Player {0} ", player.getPlayerId());
    }

  }

}
