package com.redhat.developers.data;

import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.json.bind.Jsonb;

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

@ApplicationScoped
public class LeaderBoardAggregator {

  Logger logger = Logger.getLogger(LeaderBoardAggregator.class.getName());

  @ConfigProperty(name = "quarkus.kafka-streams.topics")
  List<String> topics;

  @ConfigProperty(name = "rhdemo.leaderboard-kvstore-name")
  String kvStoreName;

  @ConfigProperty(name = "rhdemo.leaderboard-aggregator-stream")
  String outStream;
 
  @Inject
  Jsonb jsonb;
  
  @Produces
  public Topology buildLeaderBoard() {
    JsonbSerde<Player> playerSerde = new JsonbSerde<>(Player.class);
    JsonbSerde<LeaderBoardItem> leaderBoardSerde = new JsonbSerde<>(LeaderBoardItem.class);
    
    StreamsBuilder builder = new StreamsBuilder();
    
    KeyValueBytesStoreSupplier storeSupplier = 
        Stores.persistentKeyValueStore(kvStoreName);

    KStream<String, Player> source = builder.stream(topics,
        (Consumed.with(Serdes.String(),playerSerde)));
    //TODO Window?
    source
        .groupBy((k, v) -> v.getId())
        .aggregate(LeaderBoardItem::new, 
            (pId, player, aggregation) -> aggregation.updateFrom(player),
            Materialized.<String,LeaderBoardItem> as(storeSupplier)
             .withValueSerde(leaderBoardSerde))
        .toStream()
        .to(outStream,
           Produced.with(Serdes.String(), leaderBoardSerde));
    
    return builder.build();
  }
}