package com.redhat.developers.streams;

import java.util.List;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import com.redhat.developers.data.Avatar;
import com.redhat.developers.data.Player;
import com.redhat.developers.data.ScoringKafkaMessage;
import com.redhat.developers.data.Transaction;
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

/**
 * 
 */
@ApplicationScoped
public class LeaderBoardAggregator {

  Logger logger = Logger.getLogger(LeaderBoardAggregator.class.getName());

  @ConfigProperty(name = "quarkus.kafka-streams.topics")
  List<String> topics;

  @ConfigProperty(name = "rhdemo.leaderboard.kvstore.name")
  String kvStoreName;

  @ConfigProperty(name = "rhdemo.leaderboard.aggregator.stream")
  String outStream;


  /**
   * 
   * @return
   */
  @Produces
  public Topology buildLeaderBoard() {
    JsonbSerde<ScoringKafkaMessage> scoringKafkaMessageSerde =
        new JsonbSerde<>(ScoringKafkaMessage.class);
    // JsonbSerde<Game> gameSerde = new JsonbSerde<>(Game.class);
    JsonbSerde<Player> playerSerde = new JsonbSerde<>(Player.class);

    StreamsBuilder builder = new StreamsBuilder();

    KeyValueBytesStoreSupplier storeSupplier =
        Stores.persistentKeyValueStore(kvStoreName);

    builder
        .stream(topics,
            (Consumed.with(Serdes.String(), scoringKafkaMessageSerde)))
        .selectKey((k, v) -> v.getGame().getId())
        .groupBy((k, v) -> v.getPlayer().getId())
        .aggregate(() -> Player.newPlayer(), this::aggregatePlayer,
            Materialized.<String, Player>as(storeSupplier)
                .withValueSerde(playerSerde))
        .toStream().to(outStream, Produced.with(Serdes.String(), playerSerde));
    return builder.build();
  }

  /**
   * 
   * @param gameId
   * @param base
   * @param aggregatedPlayer
   * @return
   */
  private Player aggregatePlayer(String gameId, ScoringKafkaMessage base,
      Player aggregatedPlayer) {
    Player player = base.getPlayer();
    Transaction gameTransaction = base.getTransaction();
    aggregatedPlayer
        .avatar(player.getAvatar() != null ? player.getAvatar()
            : Avatar.newAvatar())
        .gameId(gameId).creationServer(player.getCreationServer())
        .gameServer(player.getGameServer())
        .scoringServer(player.getScoringServer())
        .id(player.getId()).username(player.getUsername())
        .right(player.getRight())
        .wrong(player.getWrong())
        .score(gameTransaction.correct, player.getScore());
    return aggregatedPlayer;
  }
}
