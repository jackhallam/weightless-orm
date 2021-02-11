package com.jackhallam.weightless;

import com.jackhallam.weightless.persistents.JdbcPersistentStore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcBuilder {

  private final Connection connection;

  public JdbcBuilder(String connectionString) {
    try {
      this.connection = DriverManager.getConnection(connectionString);
    } catch (SQLException e) {
      throw new WeightlessException(e);
    }
  }

  public Weightless build() {
    JdbcPersistentStore jdbcPersistentStore = new JdbcPersistentStore(connection);
    return new Weightless(jdbcPersistentStore);
  }
}
