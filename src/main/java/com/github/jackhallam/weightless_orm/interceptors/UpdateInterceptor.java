package com.github.jackhallam.weightless_orm.interceptors;

import com.github.jackhallam.weightless_orm.WeightlessORMException;
import com.github.jackhallam.weightless_orm.interceptors.handlers.ReturnHandler;
import com.github.jackhallam.weightless_orm.persistents.PersistentStore;
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
   */
  @RuntimeType
  public <T> Object intercept(@AllArguments Object[] allArguments, @Origin java.lang.reflect.Method method) {
//    ReturnType<T, S> returnType = new ReturnTypeBuilder<T, S>().method(method).build();
//    List<Parameter<?>> parameters = new ParametersBuilder().method(method).args(allArguments).build();
//    Filterer filterer = new FiltererBuilder().parameters(parameters).build();
//    PersistentStoreQuery<S> query = persistentStore.find(returnType);
//    filterer.filter(query);
//    Optional<S> found = query.findForceOptional();
//    if (!found.isPresent()) {
//      return query.find(returnType);
//    }
//    boolean deleted = persistentStore.delete(found.get());
//    if (!deleted) {
//      throw new WeightlessORMException("Could not overwrite object " + found.get().toString());
//    }
//    return persistentStore.save(parameters.get(0).getValue()).find(returnType);


    // Convert the parameters passed in to the create method to an iterable of objects to create in the persistentStore
    Iterable<T> objectsToCreateIterable = getObjectsToUpdate(method.getParameters(), allArguments);

    // The persistentStore creates the objects and returns an iterable of the created objects
    Iterable<T> updatedObjectsIterable = persistentStore.create(objectsToCreateIterable);

    // Properly return these created objects to the user
    UpdateReturnHandler<T> updateReturnHandler = new UpdateReturnHandler<>();
    return updateReturnHandler.pick((Class<Object>) method.getReturnType()).apply(updatedObjectsIterable);
  }

  /**
   * Parse the given parameters and real arguments
   */
  private <T> Iterable<T> getObjectsToUpdate(java.lang.reflect.Parameter[] parameters, Object[] allArguments) {
    // A single parameter extending iterable
    if (parameters[0].getParameterizedType() instanceof Iterable) {
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
        throw new WeightlessORMException("Error in Update");
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
