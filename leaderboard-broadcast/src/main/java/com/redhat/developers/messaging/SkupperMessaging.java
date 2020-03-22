/*-
 * #%L
 * Leaderboard Broadcast
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
        .setContainerId("leaderboard-broadcast");
  }

}
