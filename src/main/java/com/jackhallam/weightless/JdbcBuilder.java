package com.jackhallam.weightless;

import com.jackhallam.weightless.persistents.JdbcPersistentStore;
import com.jackhallam.weightless.persistents.SqlFlavor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcBuilder {

  private final Connection connection;
  private final SqlFlavor sqlFlavor;

  public JdbcBuilder(String connectionString, SqlFlavor sqlFlavor) {
    try {
      this.connection = DriverManager.getConnection(connectionString);
      this.sqlFlavor = sqlFlavor;
    } catch (SQLException e) {
      throw new WeightlessException(e);
    }
  }

  JdbcBuilder unloadedDatabase(String databaseName) {
    try {
      connection.createStatement().executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);
      connection.createStatement().execute("USE " + databaseName);
    } catch (SQLException e) {
      throw new WeightlessException(e);
    }
    return this;
  }

  public Weightless build() {
    JdbcPersistentStore jdbcPersistentStore = new JdbcPersistentStore(connection, sqlFlavor);
    return new Weightless(jdbcPersistentStore);
  }
}
