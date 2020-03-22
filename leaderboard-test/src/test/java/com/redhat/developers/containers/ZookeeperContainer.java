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
