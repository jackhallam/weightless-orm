package com.jackhallam.weightless.interceptors;

import com.jackhallam.weightless.WeightlessException;
import com.jackhallam.weightless.interceptors.handlers.ConditionHandler;
import com.jackhallam.weightless.interceptors.handlers.ReturnHandler;
import com.jackhallam.weightless.persistents.PersistentStore;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.util.Iterator;
import java.util.Optional;

public class DeleteInterceptor {

  private final PersistentStore persistentStore;

  public DeleteInterceptor(PersistentStore persistentStore) {
    this.persistentStore = persistentStore;
  }

  /**
   * Intercept a @Delete method
   *
   * @param allArguments the real values passed as arguments to the intercepted method
   * @param method       the method details
   * @param <T>          the inner type to be returned
   * @return the output of the intercepted method
   */
  @RuntimeType
  public <T> Object intercept(@AllArguments Object[] allArguments, @Origin java.lang.reflect.Method method) {
    // Build the handlers
    ConditionHandler conditionHandler = new ConditionHandler(method.getParameters(), allArguments);
    DeleteReturnHandler<T> deleteReturnHandler = new DeleteReturnHandler<>();

    // Infer the return type
    Class<T> clazz;
    try {
      clazz = (Class<T>) Class.forName(deleteReturnHandler.inferInnerTypeIfPresent((method.getGenericReturnType())).getTypeName());
    } catch (ClassNotFoundException e) {
      throw new WeightlessException(e);
    }

    // Get the iterator from the persistentStore
    Iterable<T> deletedObjectsIterable = persistentStore.delete(clazz, conditionHandler);

    // Properly return objects
    return deleteReturnHandler.pick((Class<Object>) method.getReturnType()).apply(deletedObjectsIterable);
  }

  public class DeleteReturnHandler<T> extends ReturnHandler<T> {

    @Override
    public void handleVoid(Iterable<T> tIterable) {
      throw new WeightlessException("Delete requires an object type to be returned");
    }

    /**
     * A return type of boolean/Boolean means return false on failure
     */
    @Override
    public boolean handleBoolean(Iterable<T> tIterable) {
      throw new WeightlessException("Delete requires an object type to be returned");
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
