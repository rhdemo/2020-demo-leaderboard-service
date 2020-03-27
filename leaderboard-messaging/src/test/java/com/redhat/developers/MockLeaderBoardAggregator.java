/*-
 * #%L
 * Leaderboard Aggregator Test
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
package com.redhat.developers;

import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import com.redhat.developers.data.GameMessage;
import com.redhat.developers.data.Player;
import com.redhat.developers.streams.LeaderBoardAggregator;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.kafka.client.serialization.JsonbSerde;
import io.quarkus.test.Mock;

@Mock
@ApplicationScoped
public class MockLeaderBoardAggregator extends LeaderBoardAggregator {

  Logger logger = Logger.getLogger(MockLeaderBoardAggregator.class.getName());

  @ConfigProperty(name = "quarkus.kafka-streams.topics")
  String topic;

  @ConfigProperty(name = "rhdemo.leaderboard.kvstore.name")
  String kvStoreName;

  @ConfigProperty(name = "rhdemo.leaderboard.aggregator.stream")
  String outStream;

  @Override
  @Produces
  public Topology buildLeaderBoard() {
    logger.info("Using Mock..");

    KeyValueBytesStoreSupplier storeSupplier =
        Stores
            .inMemoryKeyValueStore(kvStoreName);

    JsonbSerde<GameMessage> gameMessageSerde =
        new JsonbSerde<>(GameMessage.class);
    JsonbSerde<Player> playerSerde = new JsonbSerde<>(Player.class);
    StreamsBuilder builder = new StreamsBuilder();

    builder.stream("demo2.my-topic",
        Consumed.with(Serdes.String(), gameMessageSerde))
        .groupByKey()
        .aggregate(() -> Player.newPlayer(), this::aggregatePlayerScore,
            Materialized.<String, Player>as(storeSupplier)
                .withKeySerde(Serdes.String())
                .withValueSerde(playerSerde))
        .toStream()
        .to(outStream, Produced.with(Serdes.String(), playerSerde));
    Topology topology = builder.build();
    return topology;
  }

}
