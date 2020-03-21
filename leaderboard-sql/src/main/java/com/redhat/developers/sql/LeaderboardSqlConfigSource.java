package com.redhat.developers.sql;

import static java.util.logging.Level.FINEST;
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


}
