package com.github.jackhallam.weightless_orm;

import java.lang.annotation.*;

@Repeatable(PassTos.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PassTo {
  String fn();
  int paramNum();
}
