package com.redhat.developers.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.redhat.developers.data.LeaderBoardItem;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * LeaderBoardService
 */
@ApplicationScoped
public class LeaderBoardService {

  @Inject
  KafkaStreams streams;

  @ConfigProperty(name = "rhdemo.leaderboard-kvstore-name")
  String kvStoreName;
  
  Logger logger = Logger.getLogger(LeaderBoardService.class.getName());

  public List<LeaderBoardItem> leaderBoardWithRankings() {
    List<LeaderBoardItem> lBoardItems = new ArrayList<>();
    KeyValueIterator<String, LeaderBoardItem> itr = getLeaderBoardStore().all();
    int count = 0;
    //TODO build rank
    while (itr.hasNext() && count < 10) {
      lBoardItems.add(itr.next().value);
      count++;
    }
    return lBoardItems;
  }
  
  private ReadOnlyKeyValueStore<String, LeaderBoardItem> getLeaderBoardStore() {
        while (true) {
            try {
                return streams.store(kvStoreName, QueryableStoreTypes.keyValueStore());
            } catch (InvalidStateStoreException e) {
                // ignore, store not ready yet
            }
        }
    }
}