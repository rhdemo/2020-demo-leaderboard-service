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

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.developers.data.Game;
import com.redhat.developers.data.GameMessage;
import com.redhat.developers.data.GameState;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.codec.ReadableBuffer;
import org.apache.qpid.proton.message.Message;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
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
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.reactivex.core.Vertx;

public class ProtonClientExample extends AbstractVerticle {


  private final String MC_GAME = "mc/game";

  private static final Vertx vertx = Vertx.vertx();

  private Jsonb jsonb = JsonbBuilder.create();

  // Some GMT time
  OffsetDateTime someGMTDateTime = OffsetDateTime.of(
      LocalDateTime.of(2020, Month.MARCH, 9, 18, 01, 00),
      ZoneOffset.ofHoursMinutes(0, 0));

  public static void main(String[] args) throws Exception {
    vertx.deployVerticle(ProtonClientExample.class.getName());
  }

  public void start() throws Exception {

    GameMessage gameMessage = new GameMessage();
    Game game = Game.newGame()
        .gameId("saveTest001")
        .state(GameState.byCode(1))
        .configuration("{}")
        .date(someGMTDateTime);
    gameMessage.setType("game");
    gameMessage.setGame(game);

    ObjectMapper objectMapper = new ObjectMapper();
    Map gmap = objectMapper.readValue(jsonb.toJson(gameMessage), Map.class);
    // LinkedHashMap<String, Object> gameAsMap = new LinkedHashMap<>();
    // gameAsMap.put("id", "saveTest001");
    // gameAsMap.put("state", "active");
    // gameAsMap.put("configuration", "{}");
    // gameAsMap.put("date", someGMTDateTime);

    // LinkedHashMap<String, Object> gameMessage = new LinkedHashMap<>();
    // gameMessage.put("type", "game");
    // gameMessage.put("game", gameAsMap);

    // Use AMQP Client to send the message
    AmqpClientOptions options = clientOptions();

    AmqpClient client = AmqpClient
        .create(new io.vertx.axle.core.Vertx(vertx.getDelegate()), options);

    final io.vertx.amqp.AmqpMessage delegate =
        io.vertx.amqp.AmqpMessage.create()
            .address(MC_GAME)
            .durable(false)
            .withMapAsBody(gmap)
            .build();

    AmqpMessage message = AmqpMessage.newInstance(delegate);

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
                  System.out.println(">>>");
                  given()
                      .when().get("/api/game/saveTest001")
                      .then()
                      .statusCode(200)
                      .body(is(jsonb.toJson(game)));
                } else {
                  e.printStackTrace();
                  fail("Error sending message", e);
                }
              });
        });
  }


  private AmqpClientOptions clientOptions() throws Exception {
    AmqpClientOptions options = new AmqpClientOptions();

    String caCertPath = extracted("/ssl/ca.crt");
    String certPath = extracted("/ssl/tls.crt");
    String keyPath = extracted("/ssl/tls.key");

    PemTrustOptions pTrustOptions =
        new PemTrustOptions()
            .addCertPath(caCertPath); // ca.crt

    PemKeyCertOptions pCertOptions = new PemKeyCertOptions()
        .addCertPath(certPath.toString()) // tls.crt
        .addKeyPath(keyPath.toString()); // tls.key

    options
        .setSsl(true)
        .setConnectTimeout(30000)
        .setReconnectInterval(5000)
        .addEnabledSaslMechanism("EXTERNAL") // SASL EXTERNAL
        .setPemTrustOptions(pTrustOptions) // Trust options use ca.crt
        .setPemKeyCertOptions(pCertOptions)// Cert options use tls.crt/tls.key
        .setHostnameVerificationAlgorithm("")
        .setHost("localhost")
        .setPort(5671)
        .setContainerId("proton-client-example");
    return options;

  }


  private String extracted(String fileName) throws URISyntaxException {
    return Paths.get(this.getClass()
        .getResource(fileName).toURI())
        .toAbsolutePath().toString();
  }

}
