package com.redhat.developers.service;

import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import io.vertx.axle.pgclient.PgPool;

/**
 * GamePersistenceService
 */
@ApplicationScoped
public class GamePersistenceService {

  Logger logger = Logger.getLogger(GamePersistenceService.class.getName());

  @Inject
  PgPool client;

}
