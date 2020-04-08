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
package com.redhat.developers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import com.redhat.developers.containers.PostgreSqlContainer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * QuarkusTestEnv
 */
@SuppressWarnings("all")
public class APIQuarkusTestEnv
    implements QuarkusTestResourceLifecycleManager {

  Logger logger = Logger.getLogger(APIQuarkusTestEnv.class.getName());

  public final static String JDBC_URL =
      "jdbc:postgresql://%s:%d/gamedb";

  public static final PostgreSqlContainer postgreSQL =
      new PostgreSqlContainer();


  @Override
  public Map<String, String> start() {
    postgreSQL.start();

    Map<String, String> sysProps = new HashMap<>();

    // Quarkus
    sysProps.put("quarkus.http.test-port", "8085");
    sysProps.put("quarkus.datasource.db-kind", "postgresql");
    sysProps.put("quarkus.datasource.username", "demo");
    sysProps.put("quarkus.datasource.password", "password!");
    sysProps.put("quarkus.datasource.jdbc.url", String.format(JDBC_URL,
        "postgresql", postgreSQL.getMappedPort(5432)));

    return sysProps;
  }

  @Override
  public void stop() {
    try {
      postgreSQL.stop();
    } catch (Exception e) {

    }
  }
}
