package com.github.jackhallam.weightless_orm.persistents;

import com.github.jackhallam.weightless_orm.ReturnType;
import com.github.jackhallam.weightless_orm.WeightlessORMException;
import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Key;
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
    Class<T> clazz = (Class<T>) t.getClass();
    Key<T> key = datastore.save(t);
    java.lang.reflect.Field idField = null;

    for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
      if (field.getAnnotation(dev.morphia.annotations.Id.class) != null) {
        idField = field;
        break;
      }
    }
    if (idField == null) {
      throw new WeightlessORMException("NO ID ON " + t.getClass().getName());
    }
    return new MongoQuery<>(datastore.find(clazz).field(idField.getName()).equal(key.getId()));
  }

  public <T, S> MongoQuery<S> find(ReturnType<T, S> returnType) {
    return new MongoQuery<S>(datastore.find(returnType.getInner()));
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
