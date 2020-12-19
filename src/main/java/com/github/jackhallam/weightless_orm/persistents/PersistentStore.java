package com.github.jackhallam.weightless_orm.persistents;

import com.github.jackhallam.weightless_orm.ReturnType;

import java.io.Closeable;

public interface PersistentStore extends Closeable {
  <T> PersistentStoreQuery<T> save(T t);
  <T> boolean delete(T t);
  <T, S> PersistentStoreQuery<S> find(ReturnType<T, S> returnType);
}
