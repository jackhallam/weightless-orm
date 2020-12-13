package com.github.jackhallam.weightless_orm;

import java.lang.annotation.*;

@Repeatable(Sorts.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Sort {
  String onField();

  Direction direction() default Direction.ASCENDING;

  enum Direction {
    ASCENDING, DESCENDING
  }
}
