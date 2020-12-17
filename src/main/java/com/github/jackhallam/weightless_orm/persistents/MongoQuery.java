package com.github.jackhallam.weightless_orm.persistents;


import com.github.jackhallam.weightless_orm.ReturnType;
import com.github.jackhallam.weightless_orm.WeightlessORMException;
import com.github.jackhallam.weightless_orm.annotations.Sort;
import com.github.jackhallam.weightless_orm.annotations.field_filters.DoesNotExist;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Equals;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Exists;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Gte;
import com.github.jackhallam.weightless_orm.annotations.field_filters.HasAnyOf;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Lte;
import dev.morphia.query.FieldEnd;
import dev.morphia.query.Query;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class MongoQuery<T> implements PersistentStoreQuery<T> {

  private Query<T> query;

  public MongoQuery(Query<T> query) {
    this.query = query;
  }

  public <Outer, Inner> Outer find(ReturnType<Outer, Inner> outerReturnType) {
    Class<Outer> outerClass = outerReturnType.getOuter();
    if (outerClass.equals(List.class)) {
      return (Outer) query.find().toList();
    }
    if (outerClass.equals(Optional.class)) {
      return (Outer) Optional.ofNullable(query.iterator().tryNext());
    }
    return (Outer) query.iterator().tryNext();
  }

  public Optional<T> findForceOptional() {
    return Optional.ofNullable(query.iterator().tryNext());
  }

  public <S extends Annotation> void filter(String fieldName, S filterType, Object value) {
    if (!getFiltersMap().containsKey(filterType.annotationType())) {
      throw new WeightlessORMException("Cannot find filter " + filterType);
    }

    getFiltersMap().get(filterType.annotationType()).accept(query.field(fieldName), value);
  }

  public void sort(List<String> fieldNames, Map<String, Sort.Direction> directionMap) {
    String sortString = fieldNames.stream().map(fieldName -> {
      boolean isAscending = directionMap.get(fieldName) == Sort.Direction.ASCENDING;
      return (isAscending ? "" : "-") + fieldName;
    }).collect(Collectors.joining(","));
    if (!sortString.isEmpty()) {
      query.order(sortString);
    }
  }

  private Map<Class<? extends Annotation>, BiConsumer<FieldEnd<?>, Object>> getFiltersMap() {
    Map<Class<? extends Annotation>, BiConsumer<FieldEnd<?>, Object>> filtersMap = new HashMap<>();
    filtersMap.put(Equals.class, (fieldEnd, fieldValue) -> fieldEnd.equal(fieldValue));
    filtersMap.put(Lte.class, (fieldEnd, fieldValue) -> fieldEnd.lessThanOrEq(fieldValue));
    filtersMap.put(Gte.class, (fieldEnd, fieldValue) -> fieldEnd.greaterThanOrEq(fieldValue));
    filtersMap.put(HasAnyOf.class, (fieldEnd, fieldValue) -> fieldEnd.hasAnyOf((Iterable<?>) fieldValue));
    filtersMap.put(Exists.class, (fieldEnd, fieldValue) -> fieldEnd.exists());
    filtersMap.put(DoesNotExist.class, (fieldEnd, fieldValue) -> fieldEnd.doesNotExist());
    return filtersMap;
  }
}
