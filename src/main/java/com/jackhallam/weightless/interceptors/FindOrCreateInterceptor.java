package com.jackhallam.weightless.interceptors;

import com.jackhallam.weightless.WeightlessException;
import com.jackhallam.weightless.annotations.field_filters.Equals;
import com.jackhallam.weightless.interceptors.handlers.ConditionHandler;
import com.jackhallam.weightless.interceptors.handlers.ReturnHandler;
import com.jackhallam.weightless.interceptors.handlers.SortHandler;
import com.jackhallam.weightless.persistents.PersistentStore;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class FindOrCreateInterceptor {

  private final PersistentStore persistentStore;

  public FindOrCreateInterceptor(PersistentStore persistentStore) {
    this.persistentStore = persistentStore;
  }

  /**
   * Intercept a @FindOrCreate method
   *
   * @param allArguments the real values passed as arguments to the intercepted method
   * @param method       the method details
   * @param <T>          the inner type to be returned
   * @return the output of the intercepted method
   */
  @RuntimeType
  public <T> Object intercept(@AllArguments Object[] allArguments, @Origin java.lang.reflect.Method method) {
    SortHandler<T> sortHandler = new SortHandler<>(method);
    ConditionHandler conditionHandler = new ConditionHandler(method.getParameters(), allArguments);
    conditionHandler.getSubFiltersIterator().forEachRemaining(subFilter -> {
      if (!subFilter.filterTypeAnnotation.annotationType().equals(Equals.class)) {
        throw new WeightlessException("Only " + Equals.class + " allowed in FindOrCreate");
      }
    });
    conditionHandler = new ConditionHandler(method.getParameters(), allArguments);

    FindOrCreateReturnHandler<T> findOrCreateReturnHandler = new FindOrCreateReturnHandler<>();

    // Infer the return type
    Class<T> clazz;
    try {
      clazz = (Class<T>) Class.forName(findOrCreateReturnHandler.inferInnerTypeIfPresent((method.getGenericReturnType())).getTypeName());
    } catch (ClassNotFoundException e) {
      throw new WeightlessException(e);
    }

    // Get the iterator from the persistentStore
    Iterable<T> foundObjectsIterable = persistentStore.find(clazz, conditionHandler, sortHandler);

    if (foundObjectsIterable.iterator().hasNext()) {
      // Properly return objects
      return findOrCreateReturnHandler.pick((Class<Object>) method.getReturnType()).apply(foundObjectsIterable);
    }

    // Object not found, need to create

    conditionHandler = new ConditionHandler(method.getParameters(), allArguments);
    Map<String, Object> fields = new HashMap<>();
    conditionHandler.getSubFiltersIterator().forEachRemaining(subFilter -> {
      fields.put(subFilter.fieldName, subFilter.value);
    });

    T t = createDBObject(clazz, fields);

    Iterable<T> createdObjectsIterable = persistentStore.create(Collections.singletonList(t));

    return findOrCreateReturnHandler.pick((Class<Object>) method.getReturnType()).apply(createdObjectsIterable);
  }

  private <T> T createDBObject(Class<T> clazz, Map<String, Object> fields) {
    try {
      T t = clazz.newInstance();
      fields.forEach((fieldName, fieldValue) -> {
        try {
          java.lang.reflect.Field field = t.getClass().getDeclaredField(fieldName);
          boolean isAccessible = field.isAccessible();
          field.setAccessible(true);
          field.set(t, fieldValue);
          field.setAccessible(isAccessible);
        } catch (NoSuchFieldException | IllegalAccessException e) {
          throw new WeightlessException(e);
        }
      });
      return t;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new WeightlessException(e);
    }
  }

  public class FindOrCreateReturnHandler<T> extends ReturnHandler<T> {

    @Override
    public void handleVoid(Iterable<T> tIterable) {
      throw new WeightlessException("Void cannot be the return type of FindOrCreate");
    }

    @Override
    public boolean handleBoolean(Iterable<T> tIterable) {
      throw new WeightlessException("Boolean cannot be the return type of FindOrCreate");
    }

    @Override
    public Iterable<T> handleIterable(Iterable<T> tIterable) {
      return tIterable;
    }

    @Override
    public Optional<T> handleOptional(Iterable<T> tIterable) {
      Iterator<T> iterator = tIterable.iterator();
      if (!iterator.hasNext()) {
        return Optional.empty();
      }
      return Optional.ofNullable(iterator.next());
    }

    @Override
    public T handlePojo(Iterable<T> tIterable) {
      Iterator<T> iterator = tIterable.iterator();
      if (!iterator.hasNext()) {
        return null;
      }
      return iterator.next();
    }
  }
}
