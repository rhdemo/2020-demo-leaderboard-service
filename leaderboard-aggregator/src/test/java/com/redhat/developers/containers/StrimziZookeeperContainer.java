package com.redhat.developers.containers;

import java.nio.charset.StandardCharsets;
import com.github.dockerjava.api.command.InspectContainerResponse;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.Transferable;

public class StrimziZookeeperContainer
    extends GenericContainer<StrimziZookeeperContainer> {

  private String ZOOKEEPER_STARTER_SCRIPT = "/zookeeperStart.sh";
  private int ZOOKEEPER_PORT = 2181;

  public StrimziZookeeperContainer() {
    super("strimzi/kafka:0.16.2-kafka-2.4.0");
    withEnv("LOG_DIR", "/tmp/logs");
    withExposedPorts(ZOOKEEPER_PORT);
    super.withNetwork(Network.SHARED);
    withNetworkAliases("zookeeper");
  }

  @Override
  public void start() {
    withCommand("sh", "-c", "while [ ! -f " + ZOOKEEPER_STARTER_SCRIPT
        + " ]; do sleep 0.1; done; " + ZOOKEEPER_STARTER_SCRIPT);
    super.start();
  }

  @Override
  protected void containerIsStarting(InspectContainerResponse containerInfo,
      boolean reused) {
    super.containerIsStarting(containerInfo, reused);

    if (reused) {
      return;
    }

    StringBuffer entryPoint = new StringBuffer("#!/bin/bash");
    entryPoint.append("\n");
    entryPoint.append("export LOG_DIR=/tmp/log");
    entryPoint.append("\n");
    entryPoint.append(
        "bin/zookeeper-server-start.sh ./config/zookeeper.properties");
    entryPoint.append("\n");
    copyFileToContainer(
        Transferable.of(
            entryPoint.toString()
                .getBytes(StandardCharsets.UTF_8),
            700),
        ZOOKEEPER_STARTER_SCRIPT);
  }
}
