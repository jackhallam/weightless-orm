package com.github.jackhallam.weightless_orm.persistents;

import com.github.jackhallam.weightless_orm.ReturnType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InMemoryPersistentStore implements PersistentStore {

  private final Map<Class<?>, List<?>> mapping;

  public InMemoryPersistentStore(Map<Class<?>, List<?>> mapping) {
    this.mapping = mapping;
  }

  @Override
  public <T> InMemoryPersistentStoreQuery<T> save(T t) {
    Class<T> clazz = (Class<T>) t.getClass();
    List<T> list = (List<T>) mapping.get(clazz);
    if (list == null) {
      mapping.put(clazz, new ArrayList<>());
      list = (List<T>) mapping.get(clazz);
    }
    list.add(t);
    List<T> returnList = new ArrayList<>();
    returnList.add(t);
    return new InMemoryPersistentStoreQuery<>(clazz, returnList);
  }

  @Override
  public <T> boolean delete(T t) {
    Class<T> clazz = (Class<T>) t.getClass();
    List<T> list = (List<T>) mapping.get(clazz);
    if (list == null) {
      return false;
    }
    return list.remove(t);
  }

  @Override
  public <T, S> InMemoryPersistentStoreQuery<S> find(ReturnType<T, S> returnType) {
    Class<S> clazz = returnType.getInner();
    List<S> list = (List<S>) mapping.get(clazz);
    if (list == null) {
      return new InMemoryPersistentStoreQuery<>(clazz, new ArrayList<>());
    }
    return new InMemoryPersistentStoreQuery<>(clazz, list);
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
    // No resources to close
  }
}
