package com.redhat.developers.containers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.redhat.developers.data.GameMessage;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.Transferable;
import io.vertx.core.json.Json;

/**
 * StrimziKafkaContainer
 */
public class StrimziKafkaContainer
    extends GenericContainer<StrimziKafkaContainer> {

  private static final String STARTER_SCRIPT = "/startKafka.sh";
  private static final String DATA_LOAD_CMD = "/tmp/loadData.sh";
  private static final String DATA_FILE = "/tmp/data.txt";

  static final int KAFKA_PORT = 9093;

  int port;
  String zookeeperConnect = "zookeeper:2181";
  boolean noTopicCreate = false;
  boolean loadData = false;

  public StrimziKafkaContainer() {
    super("strimzi/kafka:0.17.0-kafka-2.4.0");
    withExposedPorts(9093);
    super.withNetwork(Network.SHARED);
    withEnv("LOG_DIR", "/tmp/logs");
    withEnv("KAFKA_LISTENERS",
        "PLAINTEXT://0.0.0.0:" + KAFKA_PORT + ",BROKER://0.0.0.0:9092");
    withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP",
        "BROKER:PLAINTEXT,PLAINTEXT:PLAINTEXT");
    withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "BROKER");

    withEnv("KAFKA_BROKER_ID", "1");
  }

  @Override
  public void start() {
    withCommand("sh", "-c", "while [ ! -f " + STARTER_SCRIPT
        + " ]; do sleep 0.1; done; " + STARTER_SCRIPT);
    super.start();
  }

  @Override
  public StrimziKafkaContainer withNetwork(Network network) {
    return super.withNetwork(network);
  }

  public StrimziKafkaContainer withLoadedData(boolean loadData) {
    this.loadData = loadData;
    return self();
  }

  public StrimziKafkaContainer withDisableTopicCreate(boolean noTopicCreate) {
    this.noTopicCreate = noTopicCreate;
    return self();
  }

  @Override
  @SuppressWarnings("all")
  protected void containerIsStarting(InspectContainerResponse containerInfo,
      boolean reused) {
    super.containerIsStarting(containerInfo, reused);

    if (reused) {
      return;
    }
    copyFileToContainer(
        Transferable.of(
            containerEntrypoint(containerInfo)
                .getBytes(StandardCharsets.UTF_8),
            700),
        STARTER_SCRIPT);
    logger().info("Loading test data ");

    String dataContent = "";
    try (InputStream in = getClass().getResource("/data.json").openStream()) {
      Jsonb jsonb = JsonbBuilder.create();
      List<GameMessage> gameMessages = jsonb.fromJson(in,
          new ArrayList<GameMessage>() {}.getClass().getGenericSuperclass());
      dataContent = gameMessages.stream()
          .map(gameMessage -> gameMessage.getPlayer().getPlayerId() + "~"
              + Json.encode(gameMessage))
          .collect(Collectors.joining("\n"));
    } catch (IOException e) {
      logger().error("Error loading data file", e);
    }
    dataContent += "\n";

    logger().debug("Data File: " + dataContent);

    StringBuffer loadCmd = new StringBuffer("#!/bin/bash");
    loadCmd.append("\n");
    loadCmd.append("bin/kafka-console-producer.sh");
    loadCmd.append(" --broker-list localhost:9092");
    loadCmd.append(" --topic my-topic");
    loadCmd.append(" --property \"parse.key=true\"");
    loadCmd.append(" --property \"key.separator=~\"");
    loadCmd.append(" < " + DATA_FILE);

    copyFileToContainer(Transferable.of(
        loadCmd.toString().getBytes(StandardCharsets.UTF_8),
        700), DATA_LOAD_CMD);

    copyFileToContainer(Transferable.of(
        dataContent.getBytes(StandardCharsets.UTF_8),
        700), DATA_FILE);
  }

  public String getBootstrapServers() {
    port = getMappedPort(KAFKA_PORT);
    return String.format("PLAINTEXT://%s:%s", getContainerIpAddress(), port);
  }

  private String containerEntrypoint(InspectContainerResponse containerInfo) {
    StringBuffer entryPoint = new StringBuffer("#!/bin/bash");
    entryPoint.append("\n");
    entryPoint.append("export KAFKA_ZOOKEEPER_CONNECT=");
    entryPoint.append("'" + this.zookeeperConnect + "'");
    entryPoint.append("\n");
    String exp = "export KAFKA_ADVERTISED_LISTENERS='" + Stream
        .concat(
            Stream.of(getBootstrapServers()),
            containerInfo.getNetworkSettings().getNetworks().values().stream()
                .map(it -> "BROKER://" + it.getIpAddress() + ":9092"))
        .collect(Collectors.joining(",")) + "'\n";
    entryPoint.append(exp);
    entryPoint.append("\n");
    entryPoint.append(" bin/kafka-server-start.sh config/server.properties");
    entryPoint.append(" --override broker.id=\"${KAFKA_BROKER_ID}\" ");
    if (this.noTopicCreate) {
      entryPoint.append(
          " --override auto.create.topics.enable=\"false\" ");
    }
    entryPoint.append(" --override listeners=\"${KAFKA_LISTENERS}\" ");
    entryPoint.append(
        " --override advertised.listeners=\"${KAFKA_ADVERTISED_LISTENERS}\"");
    entryPoint.append(
        " --override inter.broker.listener.name=\"${KAFKA_INTER_BROKER_LISTENER_NAME}\"");
    entryPoint.append(
        " --override listener.security.protocol.map=\"${KAFKA_LISTENER_SECURITY_PROTOCOL_MAP}\"");
    entryPoint
        .append(" --override zookeeper.connect=\"${KAFKA_ZOOKEEPER_CONNECT}\"");
    entryPoint.append("\n");
    return entryPoint.toString();
  }

  @Override
  protected void containerIsStarted(InspectContainerResponse containerInfo) {

    try {
      ExecResult result =
          this.execInContainer("/tmp/loadData.sh");
      if (result.getStdout() != null)
        logger().info("Data Load STDOUT:\n" + result.getStdout());
      if (result.getStderr() != null)
        logger().error("Data Load STDERR:\n" + result.getStderr());
    } catch (UnsupportedOperationException e) {
      logger().error("Error loading data into topic", e);
    } catch (IOException e) {
      logger().error("Error loading data into topic", e);
    } catch (InterruptedException e) {
      logger().error("Error loading data into topic", e);
    }
  }
}
