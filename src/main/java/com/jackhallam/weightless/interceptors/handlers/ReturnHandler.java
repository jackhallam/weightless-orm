package com.jackhallam.weightless.interceptors.handlers;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class ReturnHandler<T> {

  public abstract void handleVoid(Iterable<T> tIterable);

  public abstract boolean handleBoolean(Iterable<T> tIterable);

  public abstract Iterable<T> handleIterable(Iterable<T> tIterable);

  public abstract Optional<T> handleOptional(Iterable<T> tIterable);

  public abstract T handlePojo(Iterable<T> tIterable);

  public Type inferInnerTypeIfPresent(Type outerType) {
    Type[] types = null;
    try {
      types = ((ParameterizedType) outerType).getActualTypeArguments();
    } catch (ClassCastException e) {
      // outerType is not a ParameterizedType, it is a normal class type
      return convertPrimitiveToWrapper(outerType);
    }
    if (types.length == 0) {
      return convertPrimitiveToWrapper(outerType);
    }
    return convertPrimitiveToWrapper(Arrays.stream(((ParameterizedType) outerType).getActualTypeArguments()).findFirst().get());
  }

  private Type convertPrimitiveToWrapper(Type type) {
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

  public Function<Iterable<T>, Object> pick(Class<Object> clazz) {
    Map<Class, Function<Iterable<T>, Object>> pickMap = pickMap(this);
    if (pickMap.containsKey(clazz)) {
      return pickMap.get(clazz);
    }
    return this::handlePojo;
  }

  private Map<Class, Function<Iterable<T>, Object>> pickMap(ReturnHandler<T> returnHandler) {
    Map<Class, Function<Iterable<T>, Object>> pickMap = new HashMap<>();
    pickMap.put(void.class, tIterable -> {
      handleVoid(tIterable);
      return new Object();
    });
    pickMap.put(Void.class, tIterable -> {
      handleVoid(tIterable);
      return new Object();
    });
    pickMap.put(boolean.class, returnHandler::handleBoolean);
    pickMap.put(Boolean.class, returnHandler::handleBoolean);
    pickMap.put(Iterable.class, returnHandler::handleIterable);
    pickMap.put(Stream.class, tIterable -> StreamSupport.stream(tIterable.spliterator(), false));
    pickMap.put(Iterator.class, tIterable -> StreamSupport.stream(tIterable.spliterator(), false).iterator());
    pickMap.put(Collection.class, tIterable -> StreamSupport.stream(tIterable.spliterator(), false).collect(Collectors.toList()));
    pickMap.put(List.class, tIterable -> StreamSupport.stream(tIterable.spliterator(), false).collect(Collectors.toList()));
    pickMap.put(Optional.class, returnHandler::handleOptional);
    return pickMap;
  }
}
