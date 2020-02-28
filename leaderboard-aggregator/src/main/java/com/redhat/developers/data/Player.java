package com.redhat.developers.data;

import javax.json.bind.annotation.JsonbProperty;

/**
 * Player
 */
public class Player{
  
  @JsonbProperty("id")
  public String playerId;
  @JsonbProperty("username")
  public String playerName;
  public int right; 
  public int wrong;
  public int score;
  public String clusterSource;
  public String avatar;
  public String gameId;

}