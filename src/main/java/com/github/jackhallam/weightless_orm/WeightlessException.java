package com.github.jackhallam.weightless_orm;

public class WeightlessException extends RuntimeException {
  public WeightlessException(String message) {
    super(message);
  }

  public WeightlessException(Throwable cause) {
    super(cause);
  }
}
