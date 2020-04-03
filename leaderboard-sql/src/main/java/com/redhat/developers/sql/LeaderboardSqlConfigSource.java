/*-
 * #%L
 * Leaderboard SQL
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
package com.redhat.developers.sql;

import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * SharedDataSourceConfigSource
 */
public class LeaderboardSqlConfigSource implements ConfigSource {

  private Logger logger =
      Logger.getLogger(LeaderboardSqlConfigSource.class.getName());

  final Map<String, String> DS_MAP = new HashMap<>();

  private void init() {
    logger.log(FINEST, "Loading properties form 'leaderboard-sql.properties'");
    try {
      Properties properties = new Properties();
      properties
          .load(this.getClass()
              .getResourceAsStream("/leaderboard-sql.properties"));
      logger.log(FINEST,
          "Loaded properties form 'leaderboard-sql.properties' {0}",
          properties);
      for (String key : properties.stringPropertyNames()) {
        String value = properties.getProperty(key);
        DS_MAP.put(key, value);
      }
      logger.log(FINEST, DS_MAP.toString());
    } catch (IOException e) {
      logger.log(SEVERE,
          "Error Loading properties form 'leaderboard-sql.properties'");
    }

  }

  @Override
  public Map<String, String> getProperties() {
    init();
    return DS_MAP;
  }

  @Override
  public String getValue(String propertyName) {
    return DS_MAP.get(propertyName);
  }

  @Override
  public String getName() {
    return "leaderboard-sql-config-source";
  }

  @Override
  public int getOrdinal() {
    return 199;
  }


}
