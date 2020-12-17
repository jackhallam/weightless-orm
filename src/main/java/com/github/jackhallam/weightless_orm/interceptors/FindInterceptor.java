package com.github.jackhallam.weightless_orm.interceptors;

import com.github.jackhallam.weightless_orm.Filterer;
import com.github.jackhallam.weightless_orm.FiltererBuilder;
import com.github.jackhallam.weightless_orm.Parameter;
import com.github.jackhallam.weightless_orm.ParametersBuilder;
import com.github.jackhallam.weightless_orm.ReturnType;
import com.github.jackhallam.weightless_orm.ReturnTypeBuilder;
import com.github.jackhallam.weightless_orm.Sorter;
import com.github.jackhallam.weightless_orm.SorterBuilder;
import com.github.jackhallam.weightless_orm.persistents.PersistentStore;
import com.github.jackhallam.weightless_orm.persistents.PersistentStoreQuery;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.util.List;

public class FindInterceptor {

  private final PersistentStore persistentStore;

  public FindInterceptor(PersistentStore persistentStore) {
    this.persistentStore = persistentStore;
  }

  /**
   * Intercept a @Find method
   */
  @RuntimeType
  public <T, S> T intercept(@AllArguments Object[] allArguments, @Origin java.lang.reflect.Method method) {
    ReturnType<T, S> returnType = new ReturnTypeBuilder<T, S>().method(method).build();
    List<Parameter<?>> parameters = new ParametersBuilder().method(method).args(allArguments).build();
    Filterer filterer = new FiltererBuilder().parameters(parameters).build();
    Sorter sorter = new SorterBuilder().method(method).build();

    PersistentStoreQuery<S> query = persistentStore.find(returnType);
    filterer.filter(query);
    sorter.sort(query);
    return query.find(returnType);
  }
}
