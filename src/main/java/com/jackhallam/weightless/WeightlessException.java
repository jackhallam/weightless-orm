package com.jackhallam.weightless;

public class WeightlessException extends RuntimeException {
  public WeightlessException(String message) {
    super(message);
  }

  public WeightlessException(Throwable cause) {
    super(cause);
  }
}
