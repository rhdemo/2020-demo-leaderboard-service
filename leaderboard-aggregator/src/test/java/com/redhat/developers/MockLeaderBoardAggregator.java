package com.redhat.developers;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
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
  String topicPattern;

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

    builder.stream(Pattern.compile(topicPattern),
        Consumed.with(Serdes.String(), gameMessageSerde))
        .selectKey(
            (k, v) -> v.getPlayer().getGameId() + "~" + v.getPlayer().getId())
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
