package com.jackhallam.weightless.interceptors;

import com.jackhallam.weightless.WeightlessException;
import com.jackhallam.weightless.interceptors.handlers.ConditionHandler;
import com.jackhallam.weightless.interceptors.handlers.ReturnHandler;
import com.jackhallam.weightless.persistents.PersistentStore;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

public class UpdateInterceptor {

  private final PersistentStore persistentStore;

  public UpdateInterceptor(PersistentStore persistentStore) {
    this.persistentStore = persistentStore;
  }

  /**
   * Intercept an @Update method
   *
   * @param allArguments the real values passed as arguments to the intercepted method
   * @param method       the method details
   * @param <T>          the inner type to be returned
   * @return the output of the intercepted method
   */
  @RuntimeType
  public <T> Object intercept(@AllArguments Object[] allArguments, @Origin java.lang.reflect.Method method) {
    // Convert the parameters passed in to the update method to an iterable of objects to create in the persistentStore
    Iterable<T> objectsToUpdateIterable = getObjectsToUpdate(method.getParameters(), allArguments);

    ConditionHandler conditionHandler = new ConditionHandler(method.getParameters(), allArguments);

    // The persistentStore creates the objects and returns an iterable of the created objects
    Iterable<T> updatedObjectsIterable = persistentStore.update(objectsToUpdateIterable, conditionHandler);

    // Properly return these created objects to the user
    UpdateReturnHandler<T> updateReturnHandler = new UpdateReturnHandler<>();
    return updateReturnHandler.pick((Class<Object>) method.getReturnType()).apply(updatedObjectsIterable);
  }

  /**
   * Parse the given parameters and real arguments
   */
  private <T> Iterable<T> getObjectsToUpdate(java.lang.reflect.Parameter[] parameters, Object[] allArguments) {
    // A single parameter extending iterable
    if (Iterable.class.isAssignableFrom(parameters[0].getType())) {
      return (Iterable<T>) allArguments[0];
    }
    // A single parameter not extending iterable
    return Collections.singletonList((T) allArguments[0]);
  }

  public class UpdateReturnHandler<T> extends ReturnHandler<T> {

    /**
     * A return type of void/Void means that we should throw Exceptions on errors and return nothing on successes
     */
    @Override
    public void handleVoid(Iterable<T> tIterable) {
      if (!tIterable.iterator().hasNext()) {
        throw new WeightlessException("Error in Update");
      }
    }

    /**
     * A return type of boolean/Boolean means that we should return false on errors and return true on successes
     */
    @Override
    public boolean handleBoolean(Iterable<T> tIterable) {
      return tIterable.iterator().hasNext();
    }

    /**
     * A return type extending iterable means that we should return an iterable with the objects updated
     */
    @Override
    public Iterable<T> handleIterable(Iterable<T> tIterable) {
      return tIterable;
    }

    /**
     * A return type of optional means that we should return the first updated wrapped in an optional
     */
    @Override
    public Optional<T> handleOptional(Iterable<T> tIterable) {
      Iterator<T> iterator = tIterable.iterator();
      if (!iterator.hasNext()) {
        return Optional.empty();
      }
      return Optional.ofNullable(iterator.next());
    }

    /**
     * A return type of pojo means that we should return the first updated
     */
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
