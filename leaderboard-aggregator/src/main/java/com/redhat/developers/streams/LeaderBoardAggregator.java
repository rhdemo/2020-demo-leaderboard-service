package com.redhat.developers.streams;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Avatar;
import com.redhat.developers.data.GameMessage;
import com.redhat.developers.data.Player;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.kafka.client.serialization.JsonbSerde;

/**
 * 
 */
@ApplicationScoped
public class LeaderBoardAggregator {

  Logger logger = Logger.getLogger(LeaderBoardAggregator.class.getName());

  @ConfigProperty(name = "quarkus.kafka-streams.topics")
  String topic;

  @ConfigProperty(name = "rhdemo.leaderboard.kvstore.name")
  String kvStoreName;

  @ConfigProperty(name = "rhdemo.leaderboard.aggregator.stream")
  String outStream;

  @Inject
  Jsonb jsonb;


  @Produces
  public Topology buildLeaderBoard() {
    JsonbSerde<GameMessage> gameMessageSerde =
        new JsonbSerde<>(GameMessage.class);
    JsonbSerde<Player> playerSerde = new JsonbSerde<>(Player.class);
    StreamsBuilder builder = new StreamsBuilder();
    KeyValueBytesStoreSupplier storeSupplier =
        Stores.persistentKeyValueStore(kvStoreName);

    // logger.log(Level.FINE, "Using Topic Pattern :{0}", topicPattern);
    // validatePattern();

    KStream<String, Player> playerStream = builder
        .stream(topic,
            (Consumed.with(Serdes.String(), gameMessageSerde)))
        .groupByKey()
        .aggregate(() -> Player.newPlayer(), this::aggregatePlayerScore,
            Materialized.<String, Player>as(storeSupplier)
                .withKeySerde(Serdes.String())
                .withValueSerde(playerSerde))
        .toStream();

    playerStream.to(outStream,
        Produced.with(Serdes.String(), playerSerde));

    Topology topology = builder.build();
    logger.log(Level.FINE, topology.describe().toString());
    return topology;
  }

  /**
   * 
   */
  protected Player aggregatePlayerScore(String key, GameMessage gameMessage,
      Player aggregatedPlayer) {
    Player player = gameMessage.getPlayer();
    logger.log(Level.FINER,
        "Aggregation Key {0} and Player {1}",
        new Object[] {key, aggregatedPlayer.getPlayerId()});
    aggregatedPlayer
        .avatar(player.getAvatar() != null ? player.getAvatar()
            : Avatar.newAvatar())
        .gameId(player.getGameId())
        .creationServer(player.getCreationServer())
        .gameServer(player.getGameServer())
        .scoringServer(player.getScoringServer())
        .playerId(player.getPlayerId())
        .username(player.getUsername())
        .right(player.getRight())
        .wrong(player.getWrong())
        .score(player.getScore());
    logger.log(Level.FINER,
        "Aggregated score for player Player {0} is {1} ",
        new Object[] {aggregatedPlayer.getPlayerId(),
            aggregatedPlayer.getScore()});
    return aggregatedPlayer;
  }
}
