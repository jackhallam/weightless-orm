package com.jackhallam.weightless.persistents;

import com.jackhallam.weightless.WeightlessException;
import com.jackhallam.weightless.annotations.Sort;
import com.jackhallam.weightless.annotations.field_filters.Contains;
import com.jackhallam.weightless.annotations.field_filters.ContainsIgnoreCase;
import com.jackhallam.weightless.annotations.field_filters.DoesNotExist;
import com.jackhallam.weightless.annotations.field_filters.EndsWith;
import com.jackhallam.weightless.annotations.field_filters.EndsWithIgnoreCase;
import com.jackhallam.weightless.annotations.field_filters.Equals;
import com.jackhallam.weightless.annotations.field_filters.Exists;
import com.jackhallam.weightless.annotations.field_filters.GreaterThan;
import com.jackhallam.weightless.annotations.field_filters.GreaterThanOrEqualTo;
import com.jackhallam.weightless.annotations.field_filters.HasAnyOf;
import com.jackhallam.weightless.annotations.field_filters.HasNoneOf;
import com.jackhallam.weightless.annotations.field_filters.LessThan;
import com.jackhallam.weightless.annotations.field_filters.LessThanOrEqualTo;
import com.jackhallam.weightless.annotations.field_filters.StartsWith;
import com.jackhallam.weightless.annotations.field_filters.StartsWithIgnoreCase;
import com.jackhallam.weightless.interceptors.handlers.ConditionHandler;
import com.jackhallam.weightless.interceptors.handlers.SortHandler;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class InMemoryPersistentStore implements PersistentStore {

  private final Map<Class<?>, List<?>> mapping;

  public InMemoryPersistentStore(Map<Class<?>, List<?>> mapping) {
    this.mapping = mapping;
  }

  @Override
  public <T> Iterable<T> create(Iterable<T> tIterable) {
    List<T> saved = new ArrayList<>();
    for (T t : tIterable) {
      Class<T> clazz = (Class<T>) t.getClass();
      List<T> list = (List<T>) mapping.get(clazz);
      if (list == null) {
        mapping.put(clazz, new ArrayList<>());
        list = (List<T>) mapping.get(clazz);
      }
      list.add(t);
      saved.add(t);
    }
    return saved;
  }

  @Override
  public <T> Iterable<T> find(Class<T> clazz, ConditionHandler conditionHandler, SortHandler<T> sortHandler) {
    AtomicReference<List<T>> list = new AtomicReference<>((List<T>) mapping.get(clazz));
    if (list.get() == null) {
      return new ArrayList<>();
    }
    conditionHandler.getSubFiltersIterator().forEachRemaining(subFilter -> list.set(filter(list.get(), clazz, subFilter.fieldName, subFilter.filterTypeAnnotation, subFilter.value)));
    list.set(sort(list.get(), clazz, sortHandler.getFieldNames(), sortHandler.getDirectionMap()));
    return list.get();
  }

  @Override
  public <T> Iterable<T> update(Iterable<T> tIterable, ConditionHandler conditionHandler) {
    Iterator<T> tIterator = tIterable.iterator();
    if (!tIterator.hasNext()) {
      throw new WeightlessException("No object to use to update.");
    }

    T t = tIterator.next();

    if (tIterator.hasNext()) {
      throw new WeightlessException("Expected only one object to update but found more than one.");
    }

    Class<T> clazz = (Class<T>) t.getClass();

    Iterable<T> deletedIterable = this.delete(clazz, conditionHandler);
    Iterator<T> deletedIterator = deletedIterable.iterator();
    if (!deletedIterator.hasNext()) {
      return Collections.emptyList(); // We did not find an item to update
    }

    return create(Collections.singletonList(t)); // Convert that single object back to iterable and save it
  }

  @Override
  public <T> Iterable<T> delete(Class<T> clazz, ConditionHandler conditionHandler) {
    AtomicReference<List<T>> list = new AtomicReference<>((List<T>) mapping.get(clazz));
    if (list.get() == null) {
      return new ArrayList<>();
    }
    conditionHandler.getSubFiltersIterator().forEachRemaining(subFilter -> list.set(filter(list.get(), clazz, subFilter.fieldName, subFilter.filterTypeAnnotation, subFilter.value)));

    List<T> output = new ArrayList<>();
    list.get().forEach(item -> {
      if (mapping.get(clazz).remove(item)) {
        output.add(item);
      }
    });
    return output;
  }

  private <T, S extends Annotation> List<T> filter(List<T> input, Class<T> clazz, String fieldName, S filterType, Object value) {
    return input.stream().filter(item -> {
      try {
        java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
        boolean isAccessible = field.isAccessible();
        field.setAccessible(true);
        Object foundObject = field.get(item);
        field.setAccessible(isAccessible);
        return getFiltersMap().get(filterType.annotationType()).apply(value, foundObject);
      } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new WeightlessException(e);
      }
    }).collect(Collectors.toList());
  }

  public <T> List<T> sort(List<T> input, Class<T> clazz, List<String> fieldNames, Map<String, Sort.Direction> directionMap) {
    List<T> list = new ArrayList<>(input);
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
          if (comparison != 0) {
            return comparison;
          }
        }
        return 0;
      } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new WeightlessException(e);
      }
    };
    list.sort(comparator);
    return list;
  }

  private Map<Class<? extends Annotation>, BiFunction<Object, Object, Boolean>> getFiltersMap() {
    Map<Class<? extends Annotation>, BiFunction<Object, Object, Boolean>> filtersMap = new HashMap<>();
    filtersMap.put(Contains.class, (testerObject, dbObject) -> ((String) dbObject).contains((String) testerObject));
    filtersMap.put(ContainsIgnoreCase.class, (testerObject, dbObject) -> ((String) dbObject).toLowerCase().contains(((String) testerObject).toLowerCase()));
    filtersMap.put(DoesNotExist.class, (testerObject, dbObject) -> dbObject == null);
    filtersMap.put(EndsWith.class, (testerObject, dbObject) -> ((String) dbObject).endsWith((String) testerObject));
    filtersMap.put(EndsWithIgnoreCase.class, (testerObject, dbObject) -> ((String) dbObject).toLowerCase().endsWith(((String) testerObject).toLowerCase()));
    filtersMap.put(Equals.class, (testerObject, dbObject) -> ((Comparable<Object>) testerObject).compareTo(dbObject) == 0);
    filtersMap.put(Exists.class, (testerObject, dbObject) -> dbObject != null);
    filtersMap.put(GreaterThan.class, (testerObject, dbObject) -> ((Comparable<Object>) dbObject).compareTo(testerObject) > 0);
    filtersMap.put(GreaterThanOrEqualTo.class, (testerObject, dbObject) -> ((Comparable<Object>) dbObject).compareTo(testerObject) >= 0);
    filtersMap.put(HasAnyOf.class, (testerObject, dbObject) -> {
      for (Object o : ((Iterable<?>) testerObject)) {
        if (((Comparable<Object>) dbObject).compareTo(o) == 0) {
          return true;
        }
      }
      return false;
    });
    filtersMap.put(HasNoneOf.class, (testerObject, dbObject) -> {
      for (Object o : ((Iterable<?>) testerObject)) {
        if (((Comparable<Object>) dbObject).compareTo(o) == 0) {
          return false;
        }
      }
      return true;
    });
    filtersMap.put(LessThan.class, (testerObject, dbObject) -> ((Comparable<Object>) dbObject).compareTo(testerObject) < 0);
    filtersMap.put(LessThanOrEqualTo.class, (testerObject, dbObject) -> ((Comparable<Object>) dbObject).compareTo(testerObject) <= 0);
    filtersMap.put(StartsWith.class, (testerObject, dbObject) -> ((String) dbObject).startsWith((String) testerObject));
    filtersMap.put(StartsWithIgnoreCase.class, (testerObject, dbObject) -> ((String) dbObject).toLowerCase().startsWith(((String) testerObject).toLowerCase()));
    return filtersMap;
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
