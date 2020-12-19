package com.github.jackhallam.weightless_orm;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ReturnTypeBuilder<T, S> {

  private java.lang.reflect.Method method;

  public ReturnTypeBuilder<T, S> method(java.lang.reflect.Method method) {
    this.method = method;
    return this;
  }

  public ReturnType<T, S> build() {
    try {
      Class<T> outer = (Class<T>) convertPrimitiveToWrapper(method.getReturnType());
      Class<S> inner = (Class<S>) Class.forName(getInnerTypeIfPresent(convertPrimitiveToWrapper(method.getGenericReturnType())).getTypeName());
      return new ReturnType<>(outer, inner);
    } catch (ClassNotFoundException e) {
      throw new WeightlessORMException(e);
    }
  }

  private Type getInnerTypeIfPresent(Type outerType) throws ClassNotFoundException {
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

  private Type convertPrimitiveToWrapper(Type type) throws ClassNotFoundException {
    Map<String, Class<?>> types = new HashMap<>();
    types.put(boolean.class.getTypeName(), Boolean.class);
    types.put(char.class.getTypeName(), Character.class);
    types.put(byte.class.getTypeName(), Byte.class);
    types.put(short.class.getTypeName(), Short.class);
    types.put(int.class.getTypeName(), Integer.class);
    types.put(long.class.getTypeName(), Long.class);
    types.put(float.class.getTypeName(), Float.class);
    types.put(double.class.getTypeName(), Double.class);
    types.put(void.class.getTypeName(), Void.class);
    if (types.containsKey(type.getTypeName())) {
      return types.get(type.getTypeName());
    }
    return type;
  }
}
