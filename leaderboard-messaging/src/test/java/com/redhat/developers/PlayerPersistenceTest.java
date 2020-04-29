/*-
 * #%L
 * Leaderboard Aggregator Test
 * %%
 * Copyright (C) 2020 Red Hat Inc.,
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.redhat.developers;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.inject.Inject;
import com.redhat.developers.data.Player;
import com.redhat.developers.sql.PlayerQueries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

/**
 * LeaderBoardStreamTest
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(QuarkusTestEnv.class)
public class PlayerPersistenceTest {

  static final Logger logger =
      Logger.getLogger(PlayerPersistenceTest.class.getName());

  @Inject
  PlayerQueries playerQ;

  @Inject
  GameInitializer gameInitializer;

  AtomicInteger testRecordCount = new AtomicInteger();

  @BeforeEach
  public void checkForGame() throws Exception {
    assertTrue(gameInitializer.gameExist().isPresent());
  }

  @Test
  @Order(1)
  public void testPlayerPersistence() throws Exception {

    nap();
    List<Player> players = playerQ
        .rankPlayers(3);

    assertNotNull(players);
    assertEquals(3, players.size());
    Player aPlayer = players.get(0);
    assertNotNull(aPlayer);
    assertEquals(1, aPlayer.getPk());
    assertEquals("tom", aPlayer.getPlayerId());
    assertEquals("tom", aPlayer.getUsername());
    assertEquals(4, aPlayer.getRight());
    assertEquals(1, aPlayer.getWrong());
    assertEquals(100, aPlayer.getScore());

    aPlayer = players.get(1);
    assertNotNull(aPlayer);
    assertNotNull(aPlayer);
    assertEquals(2, aPlayer.getPk());
    assertEquals("jerry", aPlayer.getPlayerId());
    assertEquals("jerry", aPlayer.getUsername());
    assertEquals(4, aPlayer.getRight());
    assertEquals(1, aPlayer.getWrong());
    assertEquals(95, aPlayer.getScore());

    aPlayer = players.get(2);
    assertEquals(3, aPlayer.getPk());
    assertEquals("Winne", aPlayer.getPlayerId());
    assertEquals("Winne", aPlayer.getUsername());
    assertEquals(3, aPlayer.getRight());
    assertEquals(1, aPlayer.getWrong());
    assertEquals(95, aPlayer.getScore());

  }

  @Test
  @Order(2)
  public void testPlayerPersistenceWithRowCount() throws Exception {

    List<Player> players = playerQ
        .rankPlayers(1);

    assertNotNull(players);
    assertEquals(1, players.size());

    Player aPlayer = players.get(0);
    assertNotNull(aPlayer);
    assertEquals(1, aPlayer.getPk());
    assertEquals("tom", aPlayer.getPlayerId());
    assertEquals("tom", aPlayer.getUsername());
    assertEquals(4, aPlayer.getRight());
    assertEquals(1, aPlayer.getWrong());
    assertEquals(100, aPlayer.getScore());

  }

  private void nap() throws Exception {
    Thread.sleep(2000);
  }
}
