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
package com.redhat.developers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.AgroalSecurityProvider;
import io.agroal.api.security.NamePrincipal;

@ApplicationScoped
public class ConnectionUtil {

  AgroalDataSource agroalDataSource;
  Connection connection;

  @PostConstruct
  void init() throws Exception {
    try {
      AgroalDataSourceConfigurationSupplier configSuppiler =
          new AgroalDataSourceConfigurationSupplier()
              .connectionPoolConfiguration(cp -> cp
                  .connectionFactoryConfiguration(
                      cf -> cf
                          .jdbcUrl("jdbc:postgresql://localhost:5432/gamedb")
                          .principal(new NamePrincipal("demo"))
                          .credential(new FixedPassword())
                          .addSecurityProvider(new FixedPasswordProvider()))
                  .minSize(1)
                  .maxSize(1));
      if (agroalDataSource == null) {
        this.agroalDataSource = AgroalDataSource.from(configSuppiler);
        this.connection =
            this.agroalDataSource.getConnection();
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw e;
    }

  }

  public Connection getConnection() {
    return connection;
  }

  private static class FixedPassword {

    private Properties asProperties() {
      Properties properties = new Properties();
      properties.setProperty("password", "password!");
      return properties;
    }

  }

  private static class FixedPasswordProvider
      implements AgroalSecurityProvider {

    @Override
    public Properties getSecurityProperties(Object securityObject) {
      if (securityObject instanceof FixedPassword) {
        return ((FixedPassword) securityObject).asProperties();
      }
      return null;
    }
  }

}
