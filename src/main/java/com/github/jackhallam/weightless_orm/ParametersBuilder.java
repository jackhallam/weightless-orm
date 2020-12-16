package com.github.jackhallam.weightless_orm;

import java.util.ArrayList;
import java.util.List;

public class ParametersBuilder {

  private java.lang.reflect.Method method;
  private Object[] args;

  public ParametersBuilder method(java.lang.reflect.Method method) {
    this.method = method;
    return this;
  }

  public ParametersBuilder args(Object[] args) {
    this.args = args;
    return this;
  }

  public List<Parameter<?>> build() {
    List<Parameter<?>> parameters = new ArrayList<>();
    for (int i = 0; i < method.getParameters().length; i++) {
      java.lang.reflect.Parameter langParameter = method.getParameters()[i];
      Object arg = args[i];
      parameters.add(new Parameter<>(langParameter, arg));
    }
    return parameters;
  }
}
