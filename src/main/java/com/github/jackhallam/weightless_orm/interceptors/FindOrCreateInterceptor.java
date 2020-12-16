package com.github.jackhallam.weightless_orm.interceptors;

import com.github.jackhallam.weightless_orm.*;
import com.github.jackhallam.weightless_orm.annotations.Field;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Equals;
import com.github.jackhallam.weightless_orm.persistents.PersistentStore;
import com.github.jackhallam.weightless_orm.persistents.PersistentStoreQuery;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FindOrCreateInterceptor {

  private final PersistentStore persistentStore;

  public FindOrCreateInterceptor(PersistentStore persistentStore) {
    this.persistentStore = persistentStore;
  }

  /**
   * Intercept a @FindOrCreate method
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
    Optional<S> found = query.findForceOptional();
    if (found.isPresent()) {
      // Found what we were looking for, rerun with correct wrapper
      return query.find(returnType);
    }

    // not found in db, create a new object and store it

    Map<String, Object> fields = new HashMap<>();
    for (int i = 0; i < method.getParameterCount(); i++) {
      java.lang.reflect.Parameter parameter = method.getParameters()[i];
      Annotation[] parameterAnnotations = parameter.getAnnotations();
      String fieldName = null;
      for (Annotation parameterAnnotation : parameterAnnotations) {
        if (parameterAnnotation.annotationType().equals(Field.class)) {
          fieldName = ((Field) parameterAnnotation).value();
          fields.put(fieldName, null);
        } else if (parameterAnnotation.annotationType().equals(Equals.class)) {
          fields.put(fieldName, allArguments[i]);
        } else {
          fields.remove(fieldName);
        }
      }
    }
    S objectToStore = createDBObject(returnType.getInner(), fields);

    query = persistentStore.save(objectToStore);
    return query.find(returnType);
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
          throw new RuntimeException(e);
        }
      });
      return t;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
