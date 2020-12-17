package com.github.jackhallam.weightless_orm;

public class WeightlessORMException extends RuntimeException {
  public WeightlessORMException(String message) {
    super(message);
  }

  public WeightlessORMException(Throwable cause) {
    super(cause);
  }
}
