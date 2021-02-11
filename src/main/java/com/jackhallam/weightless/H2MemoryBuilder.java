package com.jackhallam.weightless;

public class H2MemoryBuilder {

  private String databaseName = "temp";

  public H2MemoryBuilder database(String databaseName) {
    this.databaseName = databaseName;
    return this;
  }

  public Weightless build() {
    JdbcBuilder jdbcBuilder = new JdbcBuilder("jdbc:h2:mem:" + databaseName);
    return jdbcBuilder.build();
  }
}
