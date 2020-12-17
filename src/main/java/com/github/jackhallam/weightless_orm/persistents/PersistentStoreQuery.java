package com.github.jackhallam.weightless_orm.persistents;

import com.github.jackhallam.weightless_orm.ReturnType;
import com.github.jackhallam.weightless_orm.annotations.Sort;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PersistentStoreQuery<T> {
  <Outer, Inner> Outer find(ReturnType<Outer, Inner> outerReturnType);
  Optional<T> findForceOptional();
  <S extends Annotation> PersistentStoreQuery<T> filter(String fieldName, S filterType, Object value);
  PersistentStoreQuery<T> sort(List<String> fieldNames, Map<String, Sort.Direction> directionMap);
}
