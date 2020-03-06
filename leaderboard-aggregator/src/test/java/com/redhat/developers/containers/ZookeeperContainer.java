package com.redhat.developers.containers;

import java.util.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

public class ZookeeperContainer
    extends GenericContainer<ZookeeperContainer> {

  private static final Logger LOGGER =
      Logger.getLogger(ZookeeperContainer.class.getName());

  private static final String STRIMZI_KAFKA_VERSION = "0.16.2-kafka-2.4.0";
  private static final String STRIMZI_KAFKA_IMAGE =
      "docker.io/strimzi/kafka:" + STRIMZI_KAFKA_VERSION;

  public ZookeeperContainer(Network network) {
    super(STRIMZI_KAFKA_IMAGE);
    addExposedPorts(2181);
    withNetwork(network);
    withNetworkAliases("zookeeper");
    withEnv("LOG_DIR", "/tmp/logs");
    withCommand("sh", "-c",
        "bin/zookeeper-server-start.sh config/zookeeper.properties");
  }

  @Override
  public void start() {
    addFixedExposedPort(2181, 2181);
    super.start();
  }
}
