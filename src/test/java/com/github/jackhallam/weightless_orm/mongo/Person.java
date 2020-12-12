package com.github.jackhallam.weightless_orm.mongo;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;

@Entity
public class Person {
  @Id
  int id;
  int favoriteNumber;
  String favoriteColor;
}
