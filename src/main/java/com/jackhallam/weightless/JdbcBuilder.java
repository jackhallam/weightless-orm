package com.jackhallam.weightless;

import com.jackhallam.weightless.persistents.JdbcPersistentStore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcBuilder {

  private Connection connection;
//  private String databaseName;

  public JdbcBuilder(String connectionString) {
    try {
      this.connection = DriverManager.getConnection(connectionString);
    } catch (SQLException e) {
      throw new WeightlessException(e);
    }
  }

//  public JdbcBuilder database(String databaseName) {
//    this.databaseName = databaseName;
//    return this;
//  }

  public Weightless build() {
//    if (databaseName == null) {
//      throw new WeightlessException("Include a database name when building Weightless instance");
//    }
    JdbcPersistentStore jdbcPersistentStore = new JdbcPersistentStore(connection);
//    try {
//      jdbcPersistentStore.connectToDatabase(databaseName);
//    } catch (SQLException e) {
//      throw new WeightlessException(e);
//    }
    return new Weightless(jdbcPersistentStore);
  }
}
