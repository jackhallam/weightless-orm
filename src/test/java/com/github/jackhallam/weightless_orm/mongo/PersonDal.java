package com.github.jackhallam.weightless_orm.mongo;

import com.github.jackhallam.weightless_orm.*;

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
  List<Person> peopleWithFavoriteColor(@Field("favoriteColor") @Equals String color);

  @Find
  List<Person> peopleWithFavoriteNumberBetween(@Field("favoriteNumber") @Gte int low, @Field("favoriteNumber") @Lte int high);

  @Find
  @Or(fn1 = "peopleWithFavoriteColor", fn2 = "peopleWithFavoriteNumberBetween")
  List<Person> peopleWithFavoriteColorOrFavoriteNumberBetween(
    @PassTo(fn = "peopleWithFavoriteColor", paramNum = 0) String color,
    @PassTo(fn = "peopleWithFavoriteNumberBetween", paramNum = 0) int low,
    @PassTo(fn = "peopleWithFavoriteNumberBetween", paramNum = 1) int high);

  @Find
  @And(fn1 = "peopleWithFavoriteColor", fn2 = "peopleWithFavoriteNumberBetween")
  List<Person> peopleWithFavoriteColorAndFavoriteNumberBetween(
    @PassTo(fn = "peopleWithFavoriteColor", paramNum = 0) String color,
    @PassTo(fn = "peopleWithFavoriteNumberBetween", paramNum = 0) int low,
    @PassTo(fn = "peopleWithFavoriteNumberBetween", paramNum = 1) int high);

  @Find
  @Sort(onField = "favoriteNumber")
  List<Person> peopleByLowestFavoriteNumber();

  @Find
  @Sort(onField = "favoriteNumber")
  @Sort(onField = "id", direction = Sort.Direction.DESCENDING)
  List<Person> peopleByFavoriteNumberAndHighestId();

  @Add
  Person addPerson(Person person);

  @Update
  Person updatePerson(Person person);
}
