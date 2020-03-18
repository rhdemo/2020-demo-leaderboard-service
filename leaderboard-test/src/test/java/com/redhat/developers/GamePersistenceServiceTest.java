package com.redhat.developers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import com.redhat.developers.data.Game;
import com.redhat.developers.data.GameState;
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
import io.vertx.axle.amqp.AmqpMessageBuilder;
import io.vertx.axle.amqp.AmqpSender;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.reactivex.core.Vertx;

/**
 * GameResourceTest
 */
@QuarkusTest
@QuarkusTestResource(QuarkusTestEnv.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GamePersistenceServiceTest {

  @Inject
  Jsonb jsonb;

  @ConfigProperty(name = "skupper.messaging.ca.cert.path")
  String caCertPath;

  @ConfigProperty(name = "skupper.messaging.cert.path")
  String certPath;

  @ConfigProperty(name = "skupper.messaging.key.path")
  String keyPath;

  private final String MC_GAME = "mc/game";

  private final Vertx vertx = Vertx.vertx();

  // Some GMT time
  OffsetDateTime someGMTDateTime = OffsetDateTime.of(
      LocalDateTime.of(2020, Month.MARCH, 9, 18, 01, 00),
      ZoneOffset.ofHoursMinutes(0, 0));


  @BeforeEach
  public void chekEnv() {
    assertNotNull("quarkus.datasource.url");
    assertNotNull("amqp-host");
    assertNotNull("amqp-port");
  }

  @Test
  @Order(1)
  public void testGameAdd() {
    Game game = Game.newGame()
        .id("saveTest001").state(GameState.active)
        .config("{}")
        .date(someGMTDateTime);

    // Use AMQP Client to send the message
    AmqpClientOptions options = clientOptions();

    AmqpClient client = AmqpClient
        .create(new io.vertx.axle.core.Vertx(vertx.getDelegate()), options);

    final AmqpMessageBuilder builder = AmqpMessage.create();
    final AmqpMessage message = builder.withBody(jsonb.toJson(game))
        .address(MC_GAME).build();

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

  @Test
  @Order(2)
  public void testGameDelete() {
    given()
        .when()
        .delete("/api/game/saveTest001")
        .then()
        .statusCode(204)
        .body(is(""));
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
        .setConnectTimeout(30000)
        .setReconnectInterval(5000)
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
