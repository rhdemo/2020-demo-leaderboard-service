package com.redhat.developers.messaging;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.BeforeDestroyed;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.inject.Inject;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.eclipse.microprofile.reactive.messaging.spi.OutgoingConnectorFactory;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;
import io.smallrye.reactive.messaging.amqp.OutgoingAmqpMetadata;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.amqp.impl.AmqpMessageBuilderImpl;
import io.vertx.axle.amqp.AmqpClient;
import io.vertx.axle.amqp.AmqpConnection;
import io.vertx.axle.amqp.AmqpMessage;
import io.vertx.axle.amqp.AmqpMessageBuilder;
import io.vertx.axle.amqp.AmqpSender;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

/**
 * An Reactive Messaging outgoing connector to send messages to Skupper AMQP Qpid dispatcher
 */
@ApplicationScoped
@Connector(SkupperAmqpsOutgoingConnector.CONNECTOR_NAME)
public class SkupperAmqpsOutgoingConnector implements OutgoingConnectorFactory {

  static final Logger logger =
      Logger
          .getLogger("skupper-amqps-outgoing-connector");

  static final String CONNECTOR_NAME = "skupper-amqps";

  @Inject
  Instance<AmqpClientOptions> clientOptions;

  @Inject
  private Instance<Vertx> instanceOfVertx;

  private boolean internalVertxInstance = false;
  private Vertx vertx;
  private final List<AmqpClient> clients = new CopyOnWriteArrayList<>();

  @PostConstruct
  void init() {
    if (instanceOfVertx == null || instanceOfVertx.isUnsatisfied()) {
      internalVertxInstance = true;
      this.vertx = Vertx.vertx();
    } else {
      this.vertx = instanceOfVertx.get();
    }
  }

  SkupperAmqpsOutgoingConnector() {
    this.vertx = null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public SubscriberBuilder<? extends Message<?>, Void> getSubscriberBuilder(
      Config config) {
    AmqpClient client = createClient(config);

    logger.log(FINEST, "Building Subscriber");

    AtomicReference<AmqpSender> sender = new AtomicReference<>();

    String configuredAddress = getAddressOrFail(config);

    return ReactiveStreams.<Message<?>>builder()
        .flatMapCompletionStage(message -> {
          AmqpSender as = sender.get();
          logger.log(FINEST, "Stream Started...");

          if (as == null) {
            return client.connect()
                .thenCompose(AmqpConnection::createAnonymousSender)
                .thenApply(s -> {
                  logger.log(FINEST, "Setting  Sender ...");
                  sender.set(s);
                  return s;
                })
                .thenCompose(s -> {
                  try {
                    logger.log(FINE,
                        "Sending message " + message.getPayload());
                    return send(s, configuredAddress, message);
                  } catch (Exception e) {
                    logger.log(Level.SEVERE, "Unable to send the message", e);
                    CompletableFuture<Message<String>> future =
                        new CompletableFuture<>();
                    future.complete(Message.of(e.getMessage()));
                    return future;
                  }
                })
                .whenComplete((m, e) -> {
                  if (e != null) {
                    logger.log(Level.SEVERE, "Unable to send the AMQP message",
                        e);
                  }
                });
          } else {
            return send(as, configuredAddress, message);
          }
        }).ignore();
  }

  /**
   * 
   * @param sender
   * @param configuredAddress
   * @param message
   * @return
   */
  private CompletionStage send(AmqpSender sender,
      String configuredAddress,
      Message<?> message) {

    AtomicReference<AmqpMessage> amqpMessage = new AtomicReference<>();

    AmqpMessage amqp = buildAqmpMessage(message);

    String actualAddress = getActualAddress(message, amqp, configuredAddress);

    if (!actualAddress.equals(amqp.address())) {
      amqpMessage.set(new io.vertx.axle.amqp.AmqpMessage(
          new AmqpMessageBuilderImpl(amqp.getDelegate())
              .address(actualAddress)
              .build()));
    }
    // No Ack required
    return CompletableFuture
        .supplyAsync(() -> {
          try {
            logger.log(FINE,
                "Sending message " + message.getPayload());
            sender.send(amqpMessage.get());
            return Message.of("SUCCESS");
          } catch (Exception e) {
            logger.log(Level.SEVERE,
                "Error while sending the message", e);
            return Message.of(e.getMessage());
          }
        });
  }

  /**
   * 
   * @param config
   * @return
   */
  private String getAddressOrFail(Config config) {
    return config.getOptionalValue("address", String.class)
        .orElseGet(
            () -> config.getOptionalValue("channel-name", String.class)
                .orElseThrow(
                    () -> new IllegalArgumentException("Address must be set")));
  }

  /**
   * 
   * @param message
   * @param amqp
   * @param configuredAddress
   * @return
   */
  private String getActualAddress(Message<?> message,
      io.vertx.axle.amqp.AmqpMessage amqp, String configuredAddress) {
    if (amqp.address() != null) {
      return amqp.address();
    }
    return message.getMetadata(OutgoingAmqpMetadata.class)
        .flatMap(o -> Optional.ofNullable(o.getAddress()))
        .orElse(configuredAddress);
  }

  /**
   * 
   * @param message
   * @return
   */
  private AmqpMessage buildAqmpMessage(Message<?> message) {
    Object payload = message.getPayload();
    AmqpMessageBuilder builder = AmqpMessage.create();
    Optional<OutgoingAmqpMetadata> metadata =
        message.getMetadata(OutgoingAmqpMetadata.class);
    // Qpid's responsibility does not handle durability
    builder.durable(false);

    // Attach payload, only handles String and JSON Payloads
    if (payload instanceof JsonArray) {
      builder.withJsonArrayAsBody((JsonArray) payload);
    } else if (payload instanceof JsonObject) {
      builder.withJsonObjectAsBody((JsonObject) payload);
    } else {
      builder.withBody(payload.toString());
    }

    builder
        .address(metadata.map(OutgoingAmqpMetadata::getAddress).orElse(null));
    builder.applicationProperties(metadata
        .map(OutgoingAmqpMetadata::getProperties).orElseGet(JsonObject::new));

    builder.contentEncoding(
        metadata.map(OutgoingAmqpMetadata::getContentEncoding)
            .orElse(null));
    builder.contentType(
        metadata.map(OutgoingAmqpMetadata::getContentType)
            .orElse(null));
    return builder.build();
  }

  /**
   * 
   * @param config
   * @return
   */
  private synchronized AmqpClient createClient(Config config) {
    AmqpClient client;
    Optional<String> clientOptionsName =
        config.getOptionalValue("client-options-name", String.class);
    if (clientOptionsName.isPresent()) {
      String optionsName = clientOptionsName.get();
      Instance<AmqpClientOptions> options =
          clientOptions.select(NamedLiteral.of(optionsName));
      if (options.isUnsatisfied()) {
        throw new IllegalStateException(
            "Cannot find a " + AmqpClientOptions.class.getName()
                + " bean named " + optionsName);
      }
      logger.log(FINE, "Creating amqp client from bean named {0}",
          optionsName);
      client = AmqpClient.create(
          new io.vertx.axle.core.Vertx(vertx.getDelegate()), options.get());
    } else {
      throw new IllegalStateException(
          "A Named bean with configured with"
              + "'client-options-name' is required");
    }

    clients.add(client);
    return client;
  }

  public void terminate(
      @Observes @BeforeDestroyed(ApplicationScoped.class) Object event) {
    if (internalVertxInstance) {
      vertx.close();
    }
  }

  @PreDestroy
  public synchronized void close() {
    clients.forEach(AmqpClient::close);
    clients.clear();
  }
}
