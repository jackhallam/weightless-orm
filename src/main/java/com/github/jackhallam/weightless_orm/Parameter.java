package com.github.jackhallam.weightless_orm;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a parameter on method in a dal class
 *
 * @param <T>
 */
public class Parameter<T> {

  private final T t;
  private final List<? extends Annotation> annotations;

  public Parameter(java.lang.reflect.Parameter langParameter, T arg) {
    t = arg;
    annotations = Arrays.asList(langParameter.getDeclaredAnnotations().clone());
  }

  public T getValue() {
    return t;
  }

  public List<? extends Annotation> getAnnotations() {
    return annotations;
  }
}
