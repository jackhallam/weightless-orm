package com.github.jackhallam.weightless_orm.mongo;

import com.github.jackhallam.weightless_orm.annotations.*;
import com.github.jackhallam.weightless_orm.annotations.field_filters.DoesNotExist;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Equals;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Gte;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Lte;

import java.util.List;
import java.util.Optional;

@Dal
public interface PersonDal {

  @Find
  List<Person> getAllPeople();

  @Find
  Optional<Person> getPerson(@Field("id") @Equals int id);

  @Find
  List<Person> peopleWithFavoriteNumber(@Field("favoriteNumber") @Equals int number);

  @Find
  List<Person> peopleWithFavoriteNumberBetween(@Field("favoriteNumber") @Gte int low, @Field("favoriteNumber") @Lte int high);

  @Find
  List<Person> peopleWithFavoriteColorAndFavoriteNumberBetween(@Field("favoriteColor") @Equals String color, @Field("favoriteNumber") @Gte int low, @Field("favoriteNumber") @Lte int high);

  @Find
  List<Person> peopleWhereFavoriteColorDoesNotExist(@Field("favoriteColor") @DoesNotExist String color);

  @Find
  @Sort(onField = "favoriteNumber")
  List<Person> peopleByLowestFavoriteNumber();

  @Find
  @Sort(onField = "favoriteNumber")
  @Sort(onField = "id", direction = Sort.Direction.DESCENDING)
  List<Person> peopleByFavoriteNumberAndHighestId();

  @Create
  Person addPerson(Person person);

  @Update
  Person updatePerson(Person person);
}
