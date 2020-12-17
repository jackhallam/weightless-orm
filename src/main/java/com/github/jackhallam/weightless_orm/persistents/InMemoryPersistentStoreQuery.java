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

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class InMemoryPersistentStoreQuery<T> implements PersistentStoreQuery<T> {

  private final Class<T> clazz;
  private List<T> list;

  public InMemoryPersistentStoreQuery(Class<T> clazz, List<T> list) {
    this.clazz = clazz;
    this.list = list;
  }

  @Override
  public <Outer, Inner> Outer find(ReturnType<Outer, Inner> outerReturnType) {
    Class<Outer> outerClass = outerReturnType.getOuter();
    if (outerClass.equals(List.class)) {
      return (Outer) list;
    }
    if (outerClass.equals(Optional.class)) {
      return (Outer) list.stream().findFirst();
    }
    Optional<Inner> op = (Optional<Inner>) list.stream().findFirst();
    return op.isPresent() ? (Outer) op.get() : null;
  }

  @Override
  public Optional<T> findForceOptional() {
    return list.stream().findFirst();
  }

  @Override
  public <S extends Annotation> void filter(String fieldName, S filterType, Object value) {
    this.list = list.stream().filter(item -> {
      try {
        java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
        boolean isAccessible = field.isAccessible();
        field.setAccessible(true);
        Object foundObject = field.get(item);
        field.setAccessible(isAccessible);
        return getFiltersMap().get(filterType.annotationType()).apply(value, foundObject);
      } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new WeightlessORMException(e);
      }
    }).collect(Collectors.toList());
  }

  @Override
  public void sort(List<String> fieldNames, Map<String, Sort.Direction> directionMap) {
    Comparator<T> comparator = (o1, o2) -> {
      try {
        for (String fieldName : fieldNames) {
          java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
          boolean isAccessible = field.isAccessible();
          field.setAccessible(true);
          T t1 = (T) field.get(o1);
          T t2 = (T) field.get(o2);
          field.setAccessible(isAccessible);

          int comparison = ((Comparable<T>) t1).compareTo(t2);
          if (directionMap.get(fieldName) == Sort.Direction.DESCENDING) {
            comparison = -comparison;
          }
          if (comparison == 0) {
            continue;
          }
          return comparison;
        }
        return 0;
      } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new WeightlessORMException(e);
      }
    };
    this.list.sort(comparator);
  }

  private Map<Class<? extends Annotation>, BiFunction<Object, Object, Boolean>> getFiltersMap() {
    Map<Class<? extends Annotation>, BiFunction<Object, Object, Boolean>> filtersMap = new HashMap<>();
    filtersMap.put(Equals.class, (testerObject, dbObject) -> ((Comparable<Object>) testerObject).compareTo(dbObject) == 0);
    filtersMap.put(Lte.class, (testerObject, dbObject) -> ((Comparable<Object>) testerObject).compareTo(dbObject) <= 0);
    filtersMap.put(Gte.class, (testerObject, dbObject) -> ((Comparable<Object>) testerObject).compareTo(dbObject) >= 0);
    filtersMap.put(HasAnyOf.class, (testerObject, dbObject) -> {
      for (Object o : ((Iterable<?>) testerObject)) {
        if (((Comparable<Object>) dbObject).compareTo(o) == 0) {
          return true;
        }
      }
      return false;
    });
    filtersMap.put(Exists.class, (testerObject, dbObject) -> dbObject != null);
    filtersMap.put(DoesNotExist.class, (testerObject, dbObject) -> dbObject == null);
    return filtersMap;
  }
}
