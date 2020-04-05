package com.redhat.developers.util;

import java.sql.Connection;
import java.sql.SQLException;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import io.agroal.api.AgroalDataSource;

@ApplicationScoped
public class ConnectionUtil {

  @Inject
  AgroalDataSource agroalDataSource;

  Connection dbConnection;

  @Named("gamedb")
  @Produces
  public Connection dbConnection() throws SQLException {
    if (dbConnection == null) {
      this.dbConnection = agroalDataSource.getConnection();
    }
    return dbConnection;
  }

  @PreDestroy
  void destroy() {

    if (dbConnection != null) {
      try {
        dbConnection.close();
      } catch (SQLException e) {
        // ignore
      }
    }
  }
}
