package com.jackhallam.weightless;

import com.jackhallam.weightless.persistents.H2SqlFlavor;

import java.nio.file.Path;

public class H2Builder {

  public MemoryBacked memoryBacked() {
    return new MemoryBacked();
  }

  public FileBacked fileBacked(Path path) {
    return new FileBacked(path);
  }

  static class MemoryBacked {
    private String databaseName = "temp";

    public MemoryBacked database(String databaseName) {
      this.databaseName = databaseName;
      return this;
    }

    public Weightless build() {
      JdbcBuilder jdbcBuilder = new JdbcBuilder("jdbc:h2:mem:" + databaseName, new H2SqlFlavor());
      return jdbcBuilder.build();
    }
  }

  static class FileBacked {
    private final Path path;

    public FileBacked(Path path) {
      this.path = path;
    }

    public Weightless build() {
      JdbcBuilder jdbcBuilder = new JdbcBuilder("jdbc:h2:" + path.toString(), new H2SqlFlavor());
      return jdbcBuilder.build();
    }
  }
}
