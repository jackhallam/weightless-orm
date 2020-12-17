package com.github.jackhallam.weightless_orm;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

public class ReturnTypeBuilder<T, S> {

  private java.lang.reflect.Method method;

  public ReturnTypeBuilder<T, S> method(java.lang.reflect.Method method) {
    this.method = method;
    return this;
  }

  public ReturnType<T, S> build() {
    Class<T> outer = (Class<T>) method.getReturnType();
    Class<S> inner;
    try {
      inner = (Class<S>) Class.forName(getInnerTypeIfPresent(method.getGenericReturnType()).getTypeName());
    } catch (ClassNotFoundException e) {
      throw new WeightlessORMException(e);
    }
    return new ReturnType<>(outer, inner);
  }

  private Type getInnerTypeIfPresent(Type outerType) {
    Type[] types = null;
    try {
      types = ((ParameterizedType) outerType).getActualTypeArguments();
    } catch (ClassCastException e) {
      // outerType is not a ParameterizedType, it is a normal class type
      return outerType;
    }
    if (types.length == 0) {
      return outerType;
    }
    return Arrays.stream(((ParameterizedType) outerType).getActualTypeArguments()).findFirst().get();
  }
}
