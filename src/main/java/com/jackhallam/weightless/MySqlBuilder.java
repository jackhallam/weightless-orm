package com.jackhallam.weightless;

import com.jackhallam.weightless.persistents.MySqlFlavor;

public class MySqlBuilder {

  private String jdbcUrl;
  private String host = "localhost";
  private String databaseName = "temp";
  private String user;
  private String password;

  public MySqlBuilder jdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
    return this;
  }

  public MySqlBuilder host(String host) {
    this.host = host;
    return this;
  }

  public MySqlBuilder database(String databaseName) {
    this.databaseName = databaseName;
    return this;
  }

  public MySqlBuilder user(String user) {
    this.user = user;
    return this;
  }

  public MySqlBuilder password(String password) {
    this.password = password;
    return this;
  }

  public Weightless build() {
    JdbcBuilder jdbcBuilder = new JdbcBuilder(jdbcUrl == null ? ("jdbc:mysql://" + host + "/" + (user == null ? "" : "?user=" + user) + (password == null ? "" : "&password=" + password)) : jdbcUrl, new MySqlFlavor());
    return jdbcBuilder.unloadedDatabase(databaseName).build();
  }
}
