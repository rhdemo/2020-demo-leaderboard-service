package com.redhat.developers.streams;

import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.json.bind.Jsonb;

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

@ApplicationScoped
public class LeaderBoardAggregator {

  Logger logger = Logger.getLogger(LeaderBoardAggregator.class.getName());

  @ConfigProperty(name = "quarkus.kafka-streams.topics")
  List<String> topics;

  @ConfigProperty(name = "rhdemo.leaderboard.kvstore.name")
  String kvStoreName;

  @ConfigProperty(name = "rhdemo.leaderboard.aggregator.stream")
  String outStream;
 
  @Inject
  Jsonb jsonb;
  
  @Produces
  public Topology buildLeaderBoard() {
    JsonbSerde<ScoringKafkaMessage> scoringKafkaMessageSerde = new JsonbSerde<>(ScoringKafkaMessage.class);
    //JsonbSerde<Game> gameSerde = new JsonbSerde<>(Game.class);
    JsonbSerde<Player> playerSerde = new JsonbSerde<>(Player.class);
    
    StreamsBuilder builder = new StreamsBuilder();
    
    KeyValueBytesStoreSupplier storeSupplier = 
        Stores.persistentKeyValueStore(kvStoreName);

    builder.stream(topics,
        (Consumed.with(Serdes.String(),scoringKafkaMessageSerde)))
        .selectKey((k,v) -> v.getGame().id)
        .groupBy((k,v) -> v.getPlayer().playerId)
        .aggregate(() -> new Player(),(key, value, aggregate) -> {
            Player tempPlayer = value.getPlayer();
            Transaction gameTransaction = value.getTransaction();
            aggregate.avatar = 
              tempPlayer.avatar!=null?tempPlayer.avatar:"{}";
            aggregate.clusterSource = 
              tempPlayer.clusterSource!=null?tempPlayer.clusterSource:"N.A";
            aggregate.playerId = tempPlayer.playerId;
            aggregate.playerName = tempPlayer.playerName;
            aggregate.right += tempPlayer.right;
            aggregate.wrong += tempPlayer.wrong;
            if(gameTransaction.correct) {
                aggregate.score  += gameTransaction.points;
            }
            aggregate.gameId = value.getGame().id;
            return aggregate;
          },Materialized.<String,Player> as(storeSupplier)
        .withValueSerde(playerSerde))
        .toStream()
        .to(outStream,
            Produced.with(Serdes.String(), playerSerde));
    
    return builder.build();
  }
  
}