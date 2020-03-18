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
