package com.github.jackhallam.weightless_orm.persistents;

import com.github.jackhallam.weightless_orm.ReturnType;
import com.github.jackhallam.weightless_orm.interceptors.handlers.ConditionHandler;
import com.github.jackhallam.weightless_orm.interceptors.handlers.SortHandler;

import java.io.Closeable;

public interface PersistentStore extends Closeable {

  <T> Iterable<T> create(Iterable<T> tIterable);
  <T> Iterable<T> find(Class<T> clazz, ConditionHandler conditionHandler, SortHandler<T> sortHandler);
  <T> Iterable<T> update(Iterable<T> tIterable, ConditionHandler conditionHandler);
  <T> Iterable<T> delete(Class<T> clazz, ConditionHandler conditionHandler);

  <T> PersistentStoreQuery<T> save(T t);
  <T> boolean delete(T t);
  <T, S> PersistentStoreQuery<S> find(ReturnType<T, S> returnType);
}
