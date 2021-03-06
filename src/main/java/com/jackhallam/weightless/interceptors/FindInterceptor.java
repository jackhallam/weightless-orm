package com.jackhallam.weightless.interceptors;

import com.jackhallam.weightless.WeightlessException;
import com.jackhallam.weightless.interceptors.handlers.ConditionHandler;
import com.jackhallam.weightless.interceptors.handlers.ReturnHandler;
import com.jackhallam.weightless.interceptors.handlers.SortHandler;
import com.jackhallam.weightless.persistents.PersistentStore;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.util.Iterator;
import java.util.Optional;

public class FindInterceptor {

  private final PersistentStore persistentStore;

  public FindInterceptor(PersistentStore persistentStore) {
    this.persistentStore = persistentStore;
  }

  /**
   * Intercept a @Find method
   *
   * @param allArguments the real values passed as arguments to the intercepted method
   * @param method       the method details
   * @param <T>          the inner type to be returned
   * @return the output of the intercepted method
   */
  @RuntimeType
  public <T> Object intercept(@AllArguments Object[] allArguments, @Origin java.lang.reflect.Method method) {
    // Build the handlers
    SortHandler<T> sortHandler = new SortHandler<>(method);
    ConditionHandler conditionHandler = new ConditionHandler(method.getParameters(), allArguments);
    FindReturnHandler<T> findReturnHandler = new FindReturnHandler<>();

    // Infer the return type
    Class<T> clazz;
    try {
      clazz = (Class<T>) Class.forName(findReturnHandler.inferInnerTypeIfPresent((method.getGenericReturnType())).getTypeName());
    } catch (ClassNotFoundException e) {
      throw new WeightlessException(e);
    }

    // Get the iterator from the persistentStore
    Iterable<T> foundObjectsIterable = persistentStore.find(clazz, conditionHandler, sortHandler);

    // Properly return objects
    return findReturnHandler.pick((Class<Object>) method.getReturnType()).apply(foundObjectsIterable);
  }

  public class FindReturnHandler<T> extends ReturnHandler<T> {

    /**
     * A return type of void/Void cannot be used
     */
    @Override
    public void handleVoid(Iterable<T> tIterable) {
      throw new WeightlessException("Void cannot be the return type of Find");
    }

    /**
     * A return type of boolean/Boolean cannot be used
     */
    @Override
    public boolean handleBoolean(Iterable<T> tIterable) {
      throw new WeightlessException("Boolean cannot be the return type of Find");
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
