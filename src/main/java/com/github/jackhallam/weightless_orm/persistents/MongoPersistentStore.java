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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

  @Override
  public <T> Iterable<T> create(Iterable<T> tIterable) {
    return save(tIterable);
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
  public <T> Iterable<T> update(Iterable<T> tIterable) {
    return save(tIterable);
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

  public <T> MongoQuery<T> save(T t) {
    datastore.save(t);
    return findItemByFields(t);
  }

  private <T> MongoQuery<T> findItemByFields(T t) {
    Class<T> clazz = (Class<T>) t.getClass();
    MongoQuery<T> mongoQuery = new MongoQuery<>(datastore.find(clazz));
    for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
      try {
        boolean isAccessible = field.isAccessible();
        field.setAccessible(true);
        mongoQuery.filter(field.getName(), () -> Equals.class, field.get(t));
        field.setAccessible(isAccessible);
      } catch (IllegalAccessException e) {
        throw new WeightlessORMException(e);
      }
    }
    return mongoQuery;
  }

  @Override
  public <T> boolean delete(T t) {
    return datastore.delete(findItemByFields(t).exposeQuery()).getN() > 0;
  }

  public <T, S> MongoQuery<S> find(ReturnType<T, S> returnType) {
    return new MongoQuery<>(datastore.find(returnType.getInner()));
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
