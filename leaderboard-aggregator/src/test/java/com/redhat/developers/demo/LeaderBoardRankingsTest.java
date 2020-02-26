package com.redhat.developers.demo;

import javax.inject.Inject;
import javax.json.bind.Jsonb;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

/**
 * LeaderBoardRankingsTest
 */
@QuarkusTest
public class LeaderBoardRankingsTest {

  @Inject
  Jsonb jsonb;
  
  @Test
  public void testRankingByScore() {
    
  }
}