package com.redhat.developers.messaging;

import static java.util.logging.Level.FINE;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;

/**
 * SkupperMessaging
 */
@ApplicationScoped
public class SkupperMessaging {

  Logger logger = Logger.getLogger(SkupperMessaging.class.getName());

  @ConfigProperty(name = "skupper.messaging.ca.cert.path")
  String caCertPath;

  @ConfigProperty(name = "skupper.messaging.cert.path")
  String certPath;

  @ConfigProperty(name = "skupper.messaging.key.path")
  String keyPath;

  @ConfigProperty(name = "amqp-host")
  String host;

  @ConfigProperty(name = "amqp-port")
  int port;

  @Produces
  @Named("skupper-amqp")
  public AmqpClientOptions clientOptions() {
    logger.log(FINE, "AMQP Configuring with host {0} and port {1} ",
        new Object[] {host,
            port});

    return new AmqpClientOptions()
        .setSsl(true)
        .addEnabledSaslMechanism("EXTERNAL") // SASL EXTERNAL
        .setPemTrustOptions(new PemTrustOptions()
            .addCertPath(caCertPath)) // Trust options use ca.crt
        .setPemKeyCertOptions(new PemKeyCertOptions()
            .addCertPath(certPath) // tls.crt
            .addKeyPath(keyPath))// Cert options use tls.crt/tls.key
        .setHostnameVerificationAlgorithm("")
        .setPort(port)
        .setHost(host)
        .setContainerId("leadeboard-service");
  }

}
