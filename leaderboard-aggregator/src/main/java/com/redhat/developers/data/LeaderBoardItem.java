package com.redhat.developers.data;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * PlayerBoard
 */
@RegisterForReflection
public class LeaderBoardItem {
  public String id;
  public String name;
  public String avatar;
  public String source;
  public int totalRights;
  public int totalWrongs;
  public int totalScore;
  //TODO work on this logic
  public int prevRanking;
  public int currentRanking;
  //0,1,2 - end, play, pause
  public int gameState;

  public LeaderBoardItem updateFrom(Player player) {
    totalRights += player.getRights();
    totalWrongs += player.getWrongs();
    totalScore += player.getScore();
    this.id = player.getId();
    this.name = player.getNickName();
    this.avatar = player.getAvatar();
    this.source = player.getSource();
    return this;
  }

}