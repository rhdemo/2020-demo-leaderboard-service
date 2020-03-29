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

import java.util.HashMap;
import java.util.Map;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.images.ImagePullPolicy;
import org.testcontainers.utility.DockerImageName;

/**
 * PostgreSqlContainer
 */
public class PostgreSqlContainer<SELF extends PostgreSqlContainer<SELF>>
    extends JdbcDatabaseContainer<SELF> {

  final private Map<String, String> env = new HashMap<>();

  public PostgreSqlContainer() {
    super("quay.io/redhatdemo/openshift-pgsql12-centos8");
    env.put("PG_USER_NAME", "demo");
    env.put("PG_USER_PASSWORD", "password!");
    env.put("PG_DATABASE", "gamedb");
    env.put("PG_NETWORK_MASK", "all");
    withImagePullPolicy(new ImagePullPolicy() {

      @Override
      public boolean shouldPull(DockerImageName imageName) {
        return false;
      }
    });
    withEnv(env);
    withInitScript("schema.sql");
    waitingFor(Wait.forListeningPort());
  }

  @Override
  public void start() {
    addFixedExposedPort(5432, 5432);
    super.start();
  }

  @Override
  protected WaitStrategy getWaitStrategy() {
    return Wait.forListeningPort();
  }

  @Override
  public String getDriverClassName() {
    return "org.postgresql.Driver";
  }

  @Override
  public String getJdbcUrl() {
    return "jdbc:postgresql://" + getContainerIpAddress() + ":"
        + getMappedPort(5432) + "/" + env.get("PG_DATABASE")
        + "?loggerLevel=OFF";
  }


  @Override
  public String getUsername() {
    return env.get("PG_USER_NAME");
  }

  @Override
  public String getPassword() {
    return env.get("PG_USER_PASSWORD");
  }

  @Override
  protected String getTestQueryString() {
    return "SELECT 1";
  }

}
