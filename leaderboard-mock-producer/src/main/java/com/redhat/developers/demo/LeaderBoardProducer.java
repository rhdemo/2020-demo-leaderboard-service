package com.redhat.developers.demo;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.redhat.developers.util.RandomPlayerUtil;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.reactivex.Flowable;

@ApplicationScoped
public class LeaderBoardProducer {

  Logger logger = Logger.getLogger(LeaderBoardProducer.class.getName());

  @ConfigProperty(name = "tick.time")
  int tickTime;

  @Inject
  RandomPlayerUtil randomPlayerUtil;

  @Outgoing("leaderboard-mirror-sg")
  public Flowable<String> leaderBoardProducerSg() {
    return Flowable.interval(tickTime, TimeUnit.SECONDS).map(tick -> {
      String player = randomPlayerUtil.generate("sg");
      logger.info("Sending player " + player);
      return player;
    });
  }

  @Outgoing("leaderboard-mirror-lon")
  public Flowable<String> leaderBoardProducerLon() {
    return Flowable.interval(tickTime, TimeUnit.SECONDS).map(tick -> {
      String player = randomPlayerUtil.generate("lon");
      logger.info("Sending player " + player);
      return player;
    });
  }

  @Outgoing("leaderboard-mirror-nyc")
  public Flowable<String> leaderBoardProducerNyc() {
    return Flowable.interval(tickTime, TimeUnit.SECONDS).map(tick -> {
      String player = randomPlayerUtil.generate("nyc");
      logger.info("Sending player " + player);
      return player;
    });
  }

  @Outgoing("leaderboard-mirror-sp")
  public Flowable<String> leaderBoardProducerSp() {
    return Flowable.interval(tickTime, TimeUnit.SECONDS).map(tick -> {
      String player = randomPlayerUtil.generate("sp");
      logger.info("Sending player " + player);
      return player;
    });
  }

  @Outgoing("leaderboard-mirror-sfo")
  public Flowable<String> leaderBoardProducerSfo() {
    return Flowable.interval(tickTime, TimeUnit.SECONDS).map(tick -> {
      String player = randomPlayerUtil.generate("sfo");
      logger.info("Sending player " + player);
      return player;
    });
  }
}