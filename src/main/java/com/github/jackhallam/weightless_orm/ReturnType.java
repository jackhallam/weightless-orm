package com.github.jackhallam.weightless_orm;

public class ReturnType<T, S> {

  private final Class<T> outer;
  private final Class<S> inner; // TODO don't need to save the inner

  public ReturnType(Class<T> outer, Class<S> inner) {
    this.outer = outer;
    this.inner = inner;
  }

  public Class<T> getOuter() {
    return outer;
  }

  public Class<S> getInner() {
    return inner;
  }
}
