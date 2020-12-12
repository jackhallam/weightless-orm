package com.github.jackhallam.weightless_orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Sort {
  String onField();

  Direction direction() default Direction.ASCENDING;

  enum Direction {
    ASCENDING, DESCENDING
  }
}
