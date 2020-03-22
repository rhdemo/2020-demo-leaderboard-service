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
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

/**
 * QdrouterContainer
 */
public class QdrouterContainer extends GenericContainer<QdrouterContainer> {

  private Logger logger = Logger.getLogger(QdrouterContainer.class.getName());
  private static final String QDROUTERD_IMAGE =
      "quay.io/interconnectedcloud/qdrouterd";

  public QdrouterContainer() {
    super(QDROUTERD_IMAGE);
    withExposedPorts(5671);
    withClasspathResourceMapping("/qdrouterd.conf",
        "/etc/qpid-dispatch/qdrouterd.conf",
        BindMode.READ_ONLY);
    withClasspathResourceMapping("/ssl/ca.crt", "/etc/messaging/ca.crt",
        BindMode.READ_ONLY);
    withClasspathResourceMapping("/ssl/tls.crt", "/etc/messaging/tls.crt",
        BindMode.READ_ONLY);
    withClasspathResourceMapping("/ssl/tls.key", "/etc/messaging/tls.key",
        BindMode.READ_ONLY);
  }

  @Override
  protected WaitStrategy getWaitStrategy() {
    return Wait.forListeningPort();
  }
}
