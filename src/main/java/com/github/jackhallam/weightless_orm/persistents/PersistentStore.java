package com.github.jackhallam.weightless_orm.persistents;

import com.github.jackhallam.weightless_orm.interceptors.handlers.ConditionHandler;
import com.github.jackhallam.weightless_orm.interceptors.handlers.SortHandler;

import java.io.Closeable;

public interface PersistentStore extends Closeable {
  <T> Iterable<T> create(Iterable<T> tIterable);

  <T> Iterable<T> find(Class<T> clazz, ConditionHandler conditionHandler, SortHandler<T> sortHandler);

  <T> Iterable<T> update(Iterable<T> tIterable, ConditionHandler conditionHandler);

  <T> Iterable<T> delete(Class<T> clazz, ConditionHandler conditionHandler);
}
