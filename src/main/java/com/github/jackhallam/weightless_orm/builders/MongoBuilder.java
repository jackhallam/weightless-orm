package com.github.jackhallam.weightless_orm.builders;

import com.github.jackhallam.weightless_orm.Weightless;
import com.github.jackhallam.weightless_orm.WeightlessException;
import com.github.jackhallam.weightless_orm.persistents.MongoPersistentStore;
import com.github.jackhallam.weightless_orm.persistents.PersistentStore;
import com.mongodb.MongoClient;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MongoBuilder {
  private MongoClient mongoClient;
  private String databaseName;

  public MongoBuilder client(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
    return this;
  }

  /**
   * <p>Set the Mongo Database name to use. The database is created if it does not exist. If this parameter is not passed...
   *   <ul>
   *    <li>... and there are no databases available to connect to, a new database with a random name will be created.</li>
   *    <li>... and there is only one database available to connect to, that single database will be chosen.</li>
   *    <li>... and there are two or more databases available to connect to, a RuntimeException will be thrown.</li>
   *   </ul>
   * </p>
   *
   * @param databaseName the database name
   * @return MongoBuilder
   */
  public MongoBuilder database(String databaseName) {
    this.databaseName = databaseName;
    return this;
  }

  public Weightless build() {
    if (databaseName == null || databaseName.isEmpty()) {
      List<String> databaseNames = StreamSupport.stream(mongoClient.listDatabaseNames().spliterator(), false).collect(Collectors.toList());
      if (databaseNames.isEmpty()) {
        this.databaseName = UUID.randomUUID().toString();
      } else if (databaseNames.size() == 1) {
        this.databaseName = databaseNames.get(0);
      } else {
        throw new WeightlessException("No database name passed and found more than one to connect to: " + databaseNames);
      }
    }
    PersistentStore persistenceStore = new MongoPersistentStore(mongoClient, databaseName);
    return new Weightless(persistenceStore);
  }
}
