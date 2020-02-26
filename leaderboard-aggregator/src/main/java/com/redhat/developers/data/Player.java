package com.redhat.developers.data;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Player
 */
@RegisterForReflection
public class Player {
  private String id;
  private String nickName;
  private int rights;
  private int wrongs;
  private int score;
  private String source;
  private String avatar;

  public String getAvatar() {
    return avatar;
  }

  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

  public String getId() {
    return id;
  }

  public String getSource() {
    return source;
  }

  public void setSource(final String source) {
    this.source = source;
  }

  public int getScore() {
    return score;
  }

  public void setScore(final int score) {
    this.score = score;
  }

  public int getWrongs() {
    return wrongs;
  }

  public void setWrongs(final int wrongs) {
    this.wrongs = wrongs;
  }

  public int getRights() {
    return rights;
  }

  public void setRights(final int rights) {
    this.rights = rights;
  }

  public String getNickName() {
    return nickName;
  }

  public void setNickName(final String nickName) {
    this.nickName = nickName;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public Player() {
  }

  public Player(String id, String nickName, int rights, int wrongs, int score, String source,
      String avatar) {
    this.id = id;
    this.nickName = nickName;
    this.rights = rights;
    this.wrongs = wrongs;
    this.score = score;
    this.source = source;
    this.avatar = avatar;
  }

}