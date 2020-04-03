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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.developers.data.Game;
import com.redhat.developers.data.GameMessage;
import com.redhat.developers.data.GameState;
import com.redhat.developers.sql.GameQueries;
import org.awaitility.Awaitility;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.axle.amqp.AmqpClient;
import io.vertx.axle.amqp.AmqpConnection;
import io.vertx.axle.amqp.AmqpMessage;
import io.vertx.axle.amqp.AmqpSender;
import io.vertx.core.json.Json;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.reactivex.core.Vertx;

/**
 * GameResourceTest
 */
@QuarkusTest
@QuarkusTestResource(QuarkusMessagingTestEnv.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GamePersistenceServiceTest {

  @Inject
  ObjectMapper objectMapper;

  @ConfigProperty(name = "skupper.messaging.ca.cert.path")
  String caCertPath;

  @ConfigProperty(name = "skupper.messaging.cert.path")
  String certPath;

  @ConfigProperty(name = "skupper.messaging.key.path")
  String keyPath;

  private final String MC_GAME = "mc/game";

  private final Vertx vertx = Vertx.vertx();

  @Inject
  GameQueries gameQueries;

  @Inject
  PgPool pgClient;

  @Test
  @Order(1)
  public void testGameAdd() throws Exception {
    GameMessage gameMessage = new GameMessage();
    Game game = Game.newGame()
        .gameId("new-game-1583157439")
        .state(GameState.byCode(1))
        .configuration("{}");
    gameMessage.setType("game");
    gameMessage.setGame(game);

    // Use AMQP Client to send the message
    AmqpClientOptions options = clientOptions();

    AmqpClient client = AmqpClient
        .create(new io.vertx.axle.core.Vertx(vertx.getDelegate()), options);

    final io.vertx.amqp.AmqpMessage delegate =
        io.vertx.amqp.AmqpMessage.create()
            .address(MC_GAME)
            .durable(false)
            .contentType("application/json")
            .withBody(Json.encode(gameMessage))
            .build();

    AmqpMessage message = AmqpMessage.newInstance(delegate);

    AtomicBoolean messageSent = new AtomicBoolean();
    AtomicReference<AmqpSender> sender = new AtomicReference<>();

    client.connect()
        .thenCompose(AmqpConnection::createAnonymousSender)
        .thenApply(s -> {
          sender.set(s);
          return s;
        })
        .thenCompose(s -> {
          return s
              .sendWithAck(message)
              .whenComplete((m, e) -> {
                if (e == null) {
                  messageSent.set(true);
                } else {
                  messageSent.set(false);
                  e.printStackTrace();
                  fail("Error sending message", e);
                }
              });
        });

    Awaitility.await().atMost(Duration.ofSeconds(10)).untilTrue(messageSent);
    Optional<Optional<Game>> optGame = gameQueries
        .findById(pgClient, 1)
        .await().asOptional().atMost(Duration.ofSeconds(10));
    assertTrue(optGame.isPresent());
    assertTrue(optGame.get().isPresent());
    Game g = optGame.get().get();
    assertNotNull(g);
    assertEquals(1, g.getPk());
    assertEquals(game.getGameId(), g.getGameId());
    assertEquals(game.getState(), g.getState());
    assertEquals(game.getConfiguration(), g.getConfiguration());
  }

  @Test
  @Order(3)
  public void testGameUpdate() throws Exception {
    GameMessage gameMessage = new GameMessage();
    Game game = Game.newGame()
        .gameId("new-game-1583157439")
        .state(GameState.byCode(2))
        .configuration("{}");
    gameMessage.setType("game");
    gameMessage.setGame(game);

    // Use AMQP Client to send the message
    AmqpClientOptions options = clientOptions();

    AmqpClient client = AmqpClient
        .create(new io.vertx.axle.core.Vertx(vertx.getDelegate()), options);

    final io.vertx.amqp.AmqpMessage delegate =
        io.vertx.amqp.AmqpMessage.create()
            .address(MC_GAME)
            .durable(false)
            .contentType("application/json")
            .withBody(Json.encode(gameMessage))
            .build();

    AmqpMessage message = AmqpMessage.newInstance(delegate);


    AtomicBoolean messageSent = new AtomicBoolean();
    AtomicReference<AmqpSender> sender = new AtomicReference<>();

    client.connect()
        .thenCompose(AmqpConnection::createAnonymousSender)
        .thenApply(s -> {
          sender.set(s);
          return s;
        })
        .thenCompose(s -> {
          return s
              .sendWithAck(message)
              .whenComplete((m, e) -> {
                if (e == null) {
                  messageSent.set(true);
                } else {
                  messageSent.set(false);
                  e.printStackTrace();
                  fail("Error sending message", e);
                }
              });
        });

    Awaitility.await().atMost(Duration.ofSeconds(10)).untilTrue(messageSent);
    Optional<Optional<Game>> optGame = gameQueries
        .findById(pgClient, 1)
        .await().asOptional().atMost(Duration.ofSeconds(10));
    assertTrue(optGame.isPresent());
    assertTrue(optGame.get().isPresent());
    Game g = optGame.get().get();
    assertNotNull(g);
    assertEquals(1, g.getPk());
    assertEquals(game.getGameId(), g.getGameId());
    assertEquals(game.getState(), g.getState());
    assertEquals(game.getConfiguration(), g.getConfiguration());
  }

  @Test
  @Order(3)
  public void testGameDelete() throws Exception {
    Optional<List<Game>> games = gameQueries
        .findAll(pgClient)
        .await().asOptional().atMost(Duration.ofSeconds(10));
    games.get().forEach(g -> gameQueries.delete(pgClient, g.getPk()));
  }


  private AmqpClientOptions clientOptions() {
    AmqpClientOptions options = new AmqpClientOptions();

    PemTrustOptions pTrustOptions =
        new PemTrustOptions()
            .addCertPath(caCertPath); // ca.crt

    PemKeyCertOptions pCertOptions = new PemKeyCertOptions()
        .addCertPath(certPath) // tls.crt
        .addKeyPath(keyPath); // tls.key

    options
        .setSsl(true)
        .addEnabledSaslMechanism("EXTERNAL") // SASL EXTERNAL
        .setPemTrustOptions(pTrustOptions) // Trust options use ca.crt
        .setPemKeyCertOptions(pCertOptions)// Cert options use tls.crt/tls.key
        .setHostnameVerificationAlgorithm("")
        .setPort(Integer.parseInt(System.getProperty("amqp-port")))
        .setHost(System.getProperty("amqp-host"))
        .setContainerId("game-msg-test");
    return options;

  }

}
