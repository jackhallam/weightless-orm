<p align="center">
<img width="150" height="150" src="weightlessicon.png" title="windy by K available at https://thenounproject.com/k4dezign/collection/weather/?i=455834. CC 3.0 BY licensed (http://creativecommons.org/licenses/by/3.0/)">
</p>

<h1 align="center">Weightless</h1>

[![Github Code](https://img.shields.io/github/languages/top/jackhallam/weightless-orm?logo=github&logoColor=lightgrey)](https://github.com/jackhallam/weightless-orm)
[![Travis CI Build Status](https://img.shields.io/travis/com/jackhallam/weightless-orm?logo=Travis-CI&logoColor=lightgrey)](https://travis-ci.com/jackhallam/weightless-orm)
[![Codacy Badge](https://img.shields.io/codacy/grade/87dafa74154349a0af3878b3435b0f98?logo=codacy&logoColor=lightgrey)](https://app.codacy.com/gh/jackhallam/weightless-orm?utm_source=github.com&utm_medium=referral&utm_content=jackhallam/weightless-orm&utm_campaign=Badge_Grade)
[![CodeFactor Code Quality](https://img.shields.io/codefactor/grade/github/jackhallam/weightless-orm?logo=codefactor&logoColor=lightgrey)](https://www.codefactor.io/repository/github/jackhallam/weightless-orm)
[![LGTM Code Quality](https://img.shields.io/lgtm/grade/java/github/jackhallam/weightless-orm?label=code%20quality&logo=lgtm&logoColor=lightgrey)](https://lgtm.com/projects/g/jackhallam/weightless-orm)
[![LGTM Alerts](https://img.shields.io/lgtm/alerts/github/jackhallam/weightless-orm?label=alerts&logo=lgtm&logoColor=lightgrey)](https://lgtm.com/projects/g/jackhallam/weightless-orm)
[![Code Climate Technical Debt](https://img.shields.io/codeclimate/tech-debt/jackhallam/weightless-orm?logo=code-climate&logoColor=lightgrey)](https://codeclimate.com/github/jackhallam/weightless-orm)
[![Code Climate Maintainability](https://img.shields.io/codeclimate/maintainability/jackhallam/weightless-orm?logo=code-climate&logoColor=lightgrey)](https://codeclimate.com/github/jackhallam/weightless-orm)
[![Code Climate Issues](https://img.shields.io/codeclimate/issues/jackhallam/weightless-orm?logo=code-climate&logoColor=lightgrey)](https://codeclimate.com/github/jackhallam/weightless-orm)
[![codecov](https://img.shields.io/codecov/c/gh/jackhallam/weightless-orm?logo=codecov&logoColor=lightgrey&token=LP3NP6IVS6)](https://codecov.io/gh/jackhallam/weightless-orm)

## About
Weightless is a [object-relational](https://en.wikipedia.org/wiki/Object%E2%80%93relational_mapping) and [object-document](https://en.wikipedia.org/wiki/Document-oriented_database) mapping library for Java. Save and query Java objects in a database in just a few lines of code. Weightless is still in development, and currently supports MongoDB and a generic in-memory store for testing.

## Getting Started

### Installation
Add Weightless to your project with [Jitpack](https://jitpack.io/#jackhallam/weightless-orm).
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```
```xml
<dependency>
  <groupId>com.github.jackhallam</groupId>
  <artifactId>weightless-orm</artifactId>
  <version>see Jitpack for latest version</version>
</dependency>
```

### Basic Usage
Assume we have a `Person` object we want to read and write to a database
```java
class Person {
  String name;
  int age;
}
```
We can define a `PersonAccess` interface that describes how to access `Person` objects
```java
interface PersonAccess {
  @Create
  void create(Person person);
  
  @Find
  Person findByName(@Field("name") @Equals String name);
}
```
We never created a class that implemented the interface `PersonAccess`. Instead, the Weightless library creates the concrete implementation at runtime. Magic! 🪄✨
```java
Weightless weightless = WeightlessORMBuilder.inMemory().build(); // In-Memory database for local testing
PersonAccess personAccess = weightless.get(PersonAccess.class);

Person james = new Person("James", 30);
personAccess.create(james);

personAccess.findByName("James"); // { "name": "James", "age": 30 }
```

### Advanced Usage

```java
/**
 * Let's look at the different features of the access objects
 */
interface PersonAccess {
  ...

  // Simple usage of @Create
  @Create
  void create(Person person);

  // Return a boolean representing if the object was created or not
  @Create
  boolean toCreateOrNotToCreate(Person person); // Note: the names of these methods can be anything, its the annotation (i.e. @Create) that matters

  // Return the created object
  @Create
  Person create(Person person);

  // Return an Optional wrapping the created object
  @Create
  Optional<Person> createOptional(Person person);

  // Provide multiple Person objects and return multiple Person objects
  // - Any object extending Iterable is acceptable as the input
  // - Iterable, Iterator, Array, List, and Stream are acceptable outputs
  @Create
  List<Person> createAll(List<Person> people);

  @Create
  Collection<Person> createAllAgain(Iterable<Person> people);

  @Create
  Stream<Person> createAllAnotherWay(Person[] people);
  
  ...

  // Simple usage of @Find
  @Find
  Person find(@Field("name") @Equals String name);

  // Filter on multiple attributes
  @Find
  Person findByNameAndAge(@Field("name") @Equals String name, @Field("age") @Equals int age);

  // Many filters are available...
  // @Contains, @ContainsIgnoreCase, @DoesNotExist, @EndsWith, @EndsWithIgnoreCase, @Equals, @Exists, @GreaterThan, @GreaterThanOrEqualTo, @HasAnyOf, @HasNoneOf, @LessThan, @LessThanOrEqualTo, @StartsWith, @StartsWithIgnoreCase
  @Find
  Person findWhereAgeLessThanOrEqualTo(@Field("age") @LessThanOrEqualTo int maxAgeInclusive);

  // Return an Optional if you care about null safety
  @Find
  Optional<Person> findOptional(@Field("name") @Equals String name);

  // Return multiple Person objects to find all
  // Iterable, Iterator, Array, List, and Stream are acceptable outputs
  @Find
  List<Person> findAllWithName(@Field("name") @Equals String name);

  // An empty filter finds all 
  @Find
  List<Person> findAll();

  // Use @Sort to order your results. It defaults to ascending
  @Find
  @Sort(onField = "age")
  List<Person> findAllWithNameSorted(@Field("name") @Equals String name);

  // @Sort descending
  @Find
  @Sort(onField = "age", direction = Sort.Direction.DESCENDING)
  List<Person> findAllWithNameSortedDescending(@Field("name") @Equals String name);

  // Multiple @Sort
  @Find
  @Sort(onField = "age")
  @Sort(onField = "name")
  List<Person> findAllSortedByAgeThenName();

  // @Sort even when you only return one object
  @Find
  @Sort(onField = "age")
  Person findByNameSorted(@Field("name") @Equals String name);
  
  ...

  // Basic usage of @Update.
  // It will find the person by their name, then overwrite that entry with the provided person object.
  @Update
  void update(Person person, @Field("name") @Equals String name);

  // Boolean represents whether the update was successful
  @Update
  boolean update(Person person, @Field("name") @Equals String name);

  // Return the person object updated
  @Update
  Person update(Person person, @Field("name") @Equals String name);

  // Return an optional of the person object updated
  @Update
  Optional<Person> updateOptional(Person person, @Field("name") @Equals String name);

  // You technically can return multiple objects, but we only update one object at a time so the List would only have one element
  @Update
  List<Person> update(Person person, @Field("name") @Equals String name);
  
  ...

  // Return the newly deleted object
  // DO NOT RETURN VOID OR BOOLEAN FOR @Delete!
  @Delete
  Person delete(@Field("name") @Equals String name);

  // Return an optional of the deleted object
  @Delete
  Optional<Person> delete(@Field("name") @Equals String name);

  // Return multiple Person objects to find all deleted
  // Iterable, Iterator, Array, List, and Stream are acceptable outputs
  @Delete
  List<Person> delete(@Field("name") @Equals String name);
  
  ...

  // Find or create the person with the fields. Only use @Equals for the filters for @FindOrCreate.
  @FindOrCreate
  Person findOrCreatePerson(@Field("name") @Equals String name, @Field("age") @Equals int age);

  ...

  // Bookmark allows you to best-effort one-at-a-time loop through all the objects of a type while keeping your place persistent across threads and service restarts.
  @Bookmark
  Person getBookmark();

  // Provide a bookmark id if you want multiple bookmarks at once.
  @Bookmark
  Person getBookmark(String bookmarkId);
}
```
