package com.redhat.developers.containers;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.github.dockerjava.api.command.InspectContainerResponse;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.Transferable;

/**
 * StrimziKafkaContainer
 */
public class StrimziKafkaContainer
    extends GenericContainer<StrimziKafkaContainer> {

  private static final String STARTER_SCRIPT = "/startKafka.sh";

  static final int KAFKA_PORT = 9093;

  int port;
  String zookeeperConnect = "zookeeper:2181";

  public StrimziKafkaContainer() {
    super("strimzi/kafka:0.16.2-kafka-2.4.0");
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

  @Override
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


}
