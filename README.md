<p align="center">
<img width="600" src="weightlesslogo.png">
</p>

Weightless is an [object-relational](https://en.wikipedia.org/wiki/Object%E2%80%93relational_mapping) / [object-document](https://en.wikipedia.org/wiki/Document-oriented_database) mapping library for Java. In other words, it helps you save Java objects to a database and query them later. Weightless currently supports [MongoDB](https://www.mongodb.com).

[![Github Code](https://img.shields.io/github/languages/top/jackhallam/weightless-orm?logo=github&logoColor=lightgrey)](https://github.com/jackhallam/weightless-orm)
[![Travis CI Build Status](https://img.shields.io/travis/com/jackhallam/weightless-orm?logo=Travis-CI&logoColor=lightgrey)](https://travis-ci.com/jackhallam/weightless-orm)
[![Codacy Badge](https://img.shields.io/codacy/grade/87dafa74154349a0af3878b3435b0f98?logo=codacy&logoColor=lightgrey)](https://app.codacy.com/gh/jackhallam/weightless-orm?utm_source=github.com&utm_medium=referral&utm_content=jackhallam/weightless-orm&utm_campaign=Badge_Grade)
[![CodeFactor Code Quality](https://img.shields.io/codefactor/grade/github/jackhallam/weightless-orm?logo=codefactor&logoColor=lightgrey)](https://www.codefactor.io/repository/github/jackhallam/weightless-orm)
[![LGTM Code Quality](https://img.shields.io/lgtm/grade/java/github/jackhallam/weightless-orm?label=code%20quality&logo=lgtm&logoColor=lightgrey)](https://lgtm.com/projects/g/jackhallam/weightless-orm)
[![codecov](https://img.shields.io/codecov/c/gh/jackhallam/weightless-orm?logo=codecov&logoColor=lightgrey&token=LP3NP6IVS6)](https://codecov.io/gh/jackhallam/weightless-orm)
[![Gitter](https://img.shields.io/gitter/room/jackhallam/weightless-orm?color=%234fb999&logo=gitter&logoColor=lightgrey)](https://gitter.im/weightless-orm)

## Getting Started
Suppose we have a `Person` object we want to store in a Mongo database.
```java
class Person {
  String name;
  int age;
}
```
With the Weightless library, the next class we will create is a `PersonAccess` interface. Note the method level and parameter level annotations.
```java
interface PersonAccess {
  @Create
  void create(Person person);
  
  @Find
  Person findByName(@Field("name") @Equals String name);
}
```
Wait! We never have to implement this interface! Weightless has enough information to implement this class for us at runtime.
```java
Weightless weightless = Weightless.mongo("mongodb://localhost:27017").build(); // Connect to a local MongoDB instance
PersonAccess personAccess = weightless.get(PersonAccess.class); // PersonAccess is implemented for us here

Person james = new Person("James", 30);
personAccess.create(james);

personAccess.findByName("James"); // { "name": "James", "age": 30 }
```

## Installation
Maven installation coming soon...

## Going Further
Visit [weightlessorm.com](https://weightlessorm.com) for a more comprehensive quickstart with live examples.
