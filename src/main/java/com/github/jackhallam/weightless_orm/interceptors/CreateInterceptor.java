package com.github.jackhallam.weightless_orm.interceptors;

import com.github.jackhallam.weightless_orm.Parameter;
import com.github.jackhallam.weightless_orm.ParametersBuilder;
import com.github.jackhallam.weightless_orm.ReturnType;
import com.github.jackhallam.weightless_orm.ReturnTypeBuilder;
import com.github.jackhallam.weightless_orm.WeightlessORMException;
import com.github.jackhallam.weightless_orm.persistents.PersistentStore;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.util.List;

public class CreateInterceptor {

  private final PersistentStore persistentStore;

  public CreateInterceptor(PersistentStore persistentStore) {
    this.persistentStore = persistentStore;
  }

  /**
   * Intercept a @Create method
   */
  @RuntimeType
  public <T, S> T intercept(@AllArguments Object[] allArguments, @Origin java.lang.reflect.Method method) {
    ReturnType<T, S> returnType = new ReturnTypeBuilder<T, S>().method(method).build();
    List<Parameter<?>> parameters = new ParametersBuilder().method(method).args(allArguments).build();
    if (parameters.size() != 1) {
      throw new WeightlessORMException("Method " + method.toGenericString() + " Expected 1 Parameter, but found " + parameters.size());
    }
    return persistentStore.save(parameters.get(0).getValue()).find(returnType);
  }
}
