package com.github.jackhallam.weightless_orm.interceptors;

import com.github.jackhallam.weightless_orm.Filterer;
import com.github.jackhallam.weightless_orm.FiltererBuilder;
import com.github.jackhallam.weightless_orm.Parameter;
import com.github.jackhallam.weightless_orm.ParametersBuilder;
import com.github.jackhallam.weightless_orm.ReturnType;
import com.github.jackhallam.weightless_orm.ReturnTypeBuilder;
import com.github.jackhallam.weightless_orm.WeightlessORMException;
import com.github.jackhallam.weightless_orm.persistents.PersistentStore;
import com.github.jackhallam.weightless_orm.persistents.PersistentStoreQuery;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.util.List;
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
  public <T, S> T intercept(@AllArguments Object[] allArguments, @Origin java.lang.reflect.Method method) {
    ReturnType<T, S> returnType = new ReturnTypeBuilder<T, S>().method(method).build();
    List<Parameter<?>> parameters = new ParametersBuilder().method(method).args(allArguments).build();
    Filterer filterer = new FiltererBuilder().parameters(parameters).build();
    PersistentStoreQuery<S> query = persistentStore.find(returnType);
    filterer.filter(query);
    Optional<S> found = query.findForceOptional();
    if (!found.isPresent()) {
      return query.find(returnType);
    }
    boolean deleted = persistentStore.delete(found.get());
    if (!deleted) {
      throw new WeightlessORMException("Could not overwrite object " + found.get().toString());
    }
    return persistentStore.save(parameters.get(0).getValue()).find(returnType);
  }
}
