package com.github.jackhallam.weightless_orm.persistents;

import com.github.jackhallam.weightless_orm.ReturnType;
import com.github.jackhallam.weightless_orm.WeightlessORMException;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Equals;
import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;

import java.io.IOException;

public class MongoPersistentStore implements PersistentStore {

  private MongoClient mongoClient;
  private Datastore datastore;

  public MongoPersistentStore(Datastore datastore) {
    this.datastore = datastore;
  }

  public MongoPersistentStore(MongoClient mongoClient, String databaseName) {
    this.mongoClient = mongoClient;
    Morphia morphia = new Morphia();
    morphia.mapPackage("");
    datastore = morphia.createDatastore(mongoClient, databaseName);
  }

  public <T> MongoQuery<T> save(T t) {
    datastore.save(t);
    return findItemByFields(t);
  }

  private <T> MongoQuery<T> findItemByFields(T t) {
    Class<T> clazz = (Class<T>) t.getClass();
    MongoQuery<T> mongoQuery = new MongoQuery<>(datastore.find(clazz));
    for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
      try {
        boolean isAccessible = field.isAccessible();
        field.setAccessible(true);
        mongoQuery.filter(field.getName(), () -> Equals.class, field.get(t));
        field.setAccessible(isAccessible);
      } catch (IllegalAccessException e) {
        throw new WeightlessORMException(e);
      }
    }
    return mongoQuery;
  }

  @Override
  public <T> boolean delete(T t) {
    return datastore.delete(findItemByFields(t).exposeQuery()).getN() > 0;
  }

  public <T, S> MongoQuery<S> find(ReturnType<T, S> returnType) {
    return new MongoQuery<>(datastore.find(returnType.getInner()));
  }

  /**
   * Closes this stream and releases any system resources associated
   * with it. If the stream is already closed then invoking this
   * method has no effect.
   *
   * <p> As noted in {@link AutoCloseable#close()}, cases where the
   * close may fail require careful attention. It is strongly advised
   * to relinquish the underlying resources and to internally
   * <em>mark</em> the {@code Closeable} as closed, prior to throwing
   * the {@code IOException}.
   *
   * @throws IOException if an I/O error occurs
   */
  @Override
  public void close() throws IOException {
    if (mongoClient != null) {
      mongoClient.close();
    }
  }
}
