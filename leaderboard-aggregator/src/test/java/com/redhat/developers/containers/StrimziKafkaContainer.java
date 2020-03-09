package com.redhat.developers.containers;

import java.util.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

/**
 * StrimziKafkaContainer
 */
public class StrimziKafkaContainer
    extends GenericContainer<StrimziKafkaContainer> {

  private static final Logger LOGGER =
      Logger.getLogger(StrimziKafkaContainer.class.getName());

  private static final String STRIMZI_KAFKA_VERSION = "0.16.2-kafka-2.4.0";
  private static final String STRIMZI_KAFKA_IMAGE =
      "docker.io/strimzi/kafka:" + STRIMZI_KAFKA_VERSION;

  private int kafkaPort = 9092;

  public StrimziKafkaContainer(Network network, ZookeeperContainer zookeeper) {
    super(STRIMZI_KAFKA_IMAGE);
    LOGGER.info("Starting Strimzi Kafka with Zookeeper");
    LOGGER.info("Strimzi Version:" + STRIMZI_KAFKA_VERSION);
    dependsOn(zookeeper);
    withExposedPorts(this.kafkaPort);
    withNetwork(network);
    withEnv("LOG_DIR", "/tmp/logs");
    withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:9092");
    withEnv("KAFKA_LISTENERS", "PLAINTEXT://0.0.0.0:9092");
    withEnv("KAFKA_ZOOKEEPER_CONNECT", "zookeeper:2181");
    withCommand(
        "sh", "-c",
        "bin/kafka-server-start.sh config/server.properties --override listeners=${KAFKA_LISTENERS} --override advertised.listeners=${KAFKA_ADVERTISED_LISTENERS} --override zookeeper.connect=${KAFKA_ZOOKEEPER_CONNECT}");
  }

  public int getKafkaPort() {
    return kafkaPort;
  }

  public StrimziKafkaContainer withKafkaPort(int kafkaPort) {
    this.kafkaPort = kafkaPort;
    return this;
  }

  public void start() {
    addFixedExposedPort(9092, 9092);
    super.start();
  }
}
