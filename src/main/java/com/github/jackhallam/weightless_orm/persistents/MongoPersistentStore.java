package com.github.jackhallam.weightless_orm.persistents;

import com.github.jackhallam.weightless_orm.WeightlessORMException;
import com.github.jackhallam.weightless_orm.annotations.Sort;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Contains;
import com.github.jackhallam.weightless_orm.annotations.field_filters.ContainsIgnoreCase;
import com.github.jackhallam.weightless_orm.annotations.field_filters.DoesNotExist;
import com.github.jackhallam.weightless_orm.annotations.field_filters.EndsWith;
import com.github.jackhallam.weightless_orm.annotations.field_filters.EndsWithIgnoreCase;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Equals;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Exists;
import com.github.jackhallam.weightless_orm.annotations.field_filters.GreaterThan;
import com.github.jackhallam.weightless_orm.annotations.field_filters.GreaterThanOrEqualTo;
import com.github.jackhallam.weightless_orm.annotations.field_filters.HasAnyOf;
import com.github.jackhallam.weightless_orm.annotations.field_filters.HasNoneOf;
import com.github.jackhallam.weightless_orm.annotations.field_filters.LessThan;
import com.github.jackhallam.weightless_orm.annotations.field_filters.LessThanOrEqualTo;
import com.github.jackhallam.weightless_orm.annotations.field_filters.StartsWith;
import com.github.jackhallam.weightless_orm.annotations.field_filters.StartsWithIgnoreCase;
import com.github.jackhallam.weightless_orm.interceptors.handlers.ConditionHandler;
import com.github.jackhallam.weightless_orm.interceptors.handlers.SortHandler;
import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.FieldEnd;
import dev.morphia.query.Query;
import dev.morphia.query.internal.MorphiaCursor;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MongoPersistentStore implements PersistentStore {

  private final MongoClient mongoClient;
  private final Datastore datastore;

  public MongoPersistentStore(MongoClient mongoClient, String databaseName) {
    this.mongoClient = mongoClient;
    Morphia morphia = new Morphia();
    morphia.mapPackage("");
    datastore = morphia.createDatastore(mongoClient, databaseName);
  }

  @Override
  public <T> Iterable<T> create(Iterable<T> tIterable) {
    return save(tIterable);
  }

  @Override
  public <T> Iterable<T> find(Class<T> clazz, ConditionHandler conditionHandler, SortHandler<T> sortHandler) {
    Query<T> query = datastore.find(clazz);

    // Apply each conditional
    conditionHandler.getSubFiltersIterator().forEachRemaining(subFilter -> getFiltersMap().get(subFilter.filterTypeAnnotation.annotationType()).accept(query.field(subFilter.fieldName), subFilter.value));

    // Apply each sort
    sortHandler.getSortsIterator().forEachRemaining(sortContainer -> {
      dev.morphia.query.Sort sort = sortContainer.direction.equals(Sort.Direction.ASCENDING) ? dev.morphia.query.Sort.ascending(sortContainer.fieldName) : dev.morphia.query.Sort.descending(sortContainer.fieldName);
      query.order(sort);
    });

    MorphiaCursor<T> morphiaCursor = query.find();

    return () -> new Iterator<T>() {
      @Override
      public boolean hasNext() {
        return morphiaCursor.hasNext();
      }

      @Override
      public T next() {
        return morphiaCursor.next();
      }
    };
  }

  @Override
  public <T> Iterable<T> update(Iterable<T> tIterable, ConditionHandler conditionHandler) {
    Iterator<T> tIterator = tIterable.iterator();
    if (!tIterator.hasNext()) {
      throw new WeightlessORMException("No object to use to update.");
    }

    T t = tIterator.next();

    if (tIterator.hasNext()) {
      throw new WeightlessORMException("Expected only one object to update but found more than one.");
    }

    Class<T> clazz = (Class<T>) t.getClass();

    this.delete(clazz, conditionHandler); // We don't check output because we don't really care how many we delete

    return save(Collections.singletonList(t)); // Convert that single object back to iterable and save it
  }

  @Override
  public <T> Iterable<T> delete(Class<T> clazz, ConditionHandler conditionHandler) {
    Query<T> query = datastore.find(clazz);

    // Apply each conditional
    conditionHandler.getSubFiltersIterator().forEachRemaining(subFilter -> getFiltersMap().get(subFilter.filterTypeAnnotation.annotationType()).accept(query.field(subFilter.fieldName), subFilter.value));

    // Save a reference to each deleted object and delete each
    List<T> deleted = new ArrayList<>();
    query.find().forEachRemaining(item -> {
      Query<T> deleteQuery = datastore.find(clazz);
      for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
        boolean isAccessible = field.isAccessible();
        field.setAccessible(true);
        try {
          deleteQuery.field(field.getName()).equal(field.get(item));
        } catch (IllegalAccessException ignored) {
        }
        field.setAccessible(isAccessible);
        int n = datastore.delete(deleteQuery).getN();
        if (n >= 1) {
          deleted.add(item);
        }
      }
    });
    return deleted;
  }


  private <T> Iterable<T> save(Iterable<T> tIterable) {
    // Keep a list of the objects
    List<T> saved = StreamSupport.stream(tIterable.spliterator(), false).collect(Collectors.toList());

    // Save the objects
    datastore.save(saved);

    Iterator<T> savedIterator = saved.iterator();

    // Provide an iterator that pulls the objects
    return () -> new Iterator<T>() {
      @Override
      public boolean hasNext() {
        return savedIterator.hasNext();
      }

      @Override
      public T next() {
        // Using the saved object
        T t = savedIterator.next();
        Class<T> clazz = (Class<T>) t.getClass();
        // Pull the object from the db by fields
        Query<T> query = datastore.find(clazz);
        for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
          boolean isAccessible = field.isAccessible();
          field.setAccessible(true);
          try {
            query.field(field.getName()).equal(field.get(t));
          } catch (IllegalAccessException ignored) {
          }
          field.setAccessible(isAccessible);
        }
        return query.find().next();
      }
    };
  }

  private Map<Class<? extends Annotation>, BiConsumer<FieldEnd<?>, Object>> getFiltersMap() {
    Map<Class<? extends Annotation>, BiConsumer<FieldEnd<?>, Object>> filtersMap = new HashMap<>();
    filtersMap.put(Contains.class, (fieldEnd, fieldValue) -> fieldEnd.contains((String)fieldValue));
    filtersMap.put(ContainsIgnoreCase.class, (fieldEnd, fieldValue) -> fieldEnd.containsIgnoreCase((String)fieldValue));
    filtersMap.put(DoesNotExist.class, (fieldEnd, fieldValue) -> fieldEnd.doesNotExist());
    filtersMap.put(EndsWith.class, (fieldEnd, fieldValue) -> fieldEnd.endsWith((String)fieldValue));
    filtersMap.put(EndsWithIgnoreCase.class, (fieldEnd, fieldValue) -> fieldEnd.endsWithIgnoreCase((String)fieldValue));
    filtersMap.put(Equals.class, (fieldEnd, fieldValue) -> fieldEnd.equal(fieldValue));
    filtersMap.put(Exists.class, (fieldEnd, fieldValue) -> fieldEnd.exists());
    filtersMap.put(GreaterThan.class, (fieldEnd, fieldValue) -> fieldEnd.greaterThan(fieldValue));
    filtersMap.put(GreaterThanOrEqualTo.class, (fieldEnd, fieldValue) -> fieldEnd.greaterThanOrEq(fieldValue));
    filtersMap.put(HasAnyOf.class, (fieldEnd, fieldValue) -> fieldEnd.hasAnyOf((Iterable<?>) fieldValue));
    filtersMap.put(HasNoneOf.class, (fieldEnd, fieldValue) -> fieldEnd.hasNoneOf((Iterable<?>) fieldValue));
    filtersMap.put(LessThan.class, (fieldEnd, fieldValue) -> fieldEnd.lessThan(fieldValue));
    filtersMap.put(LessThanOrEqualTo.class, (fieldEnd, fieldValue) -> fieldEnd.lessThanOrEq(fieldValue));
    filtersMap.put(StartsWith.class, (fieldEnd, fieldValue) -> fieldEnd.startsWith((String)fieldValue));
    filtersMap.put(StartsWithIgnoreCase.class, (fieldEnd, fieldValue) -> fieldEnd.startsWithIgnoreCase((String)fieldValue));
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
    if (mongoClient != null) {
      mongoClient.close();
    }
  }
}
