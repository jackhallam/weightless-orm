<p align="center">
<img width="600" src="weightlesslogo.png">
</p>

Weightless is the lowest barrier-to-entry database mapping library for the JVM. Weightless currently supports [MongoDB](https://www.mongodb.com). Learn more at [weightlessorm.com](https://weightlessorm.com).

[![Github Code](https://img.shields.io/github/languages/top/jackhallam/weightless-orm?logo=github&logoColor=lightgrey)](https://github.com/jackhallam/weightless-orm)
[![Maven Central](https://img.shields.io/maven-central/v/com.jackhallam/weightless-orm?label=maven&logo=apache&logoColor=lightgrey)](https://search.maven.org/artifact/com.jackhallam/weightless-orm/0.1.0-beta/jar)
[![Travis CI Build Status](https://img.shields.io/travis/com/jackhallam/weightless-orm?logo=Travis-CI&logoColor=lightgrey)](https://travis-ci.com/jackhallam/weightless-orm)
[![Codacy Badge](https://img.shields.io/codacy/grade/87dafa74154349a0af3878b3435b0f98?logo=codacy&logoColor=lightgrey)](https://app.codacy.com/gh/jackhallam/weightless-orm?utm_source=github.com&utm_medium=referral&utm_content=jackhallam/weightless-orm&utm_campaign=Badge_Grade)
[![CodeFactor Code Quality](https://img.shields.io/codefactor/grade/github/jackhallam/weightless-orm?logo=codefactor&logoColor=lightgrey)](https://www.codefactor.io/repository/github/jackhallam/weightless-orm)
[![LGTM Code Quality](https://img.shields.io/lgtm/grade/java/github/jackhallam/weightless-orm?label=code%20quality&logo=lgtm&logoColor=lightgrey)](https://lgtm.com/projects/g/jackhallam/weightless-orm)
[![codecov](https://img.shields.io/codecov/c/gh/jackhallam/weightless-orm?logo=codecov&logoColor=lightgrey&token=LP3NP6IVS6)](https://codecov.io/gh/jackhallam/weightless-orm)
[![Gitter](https://img.shields.io/gitter/room/jackhallam/weightless-orm?color=%234fb999&logo=gitter&logoColor=lightgrey)](https://gitter.im/weightless-orm/weightless-orm)

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
Weightless weightless = Weightless.mongo("mongodb://localhost:27017").database("mydatabase").build(); // Connect to a local MongoDB instance
PersonAccess personAccess = weightless.get(PersonAccess.class); // PersonAccess is implemented for us here

Person james = new Person("James", 30);
personAccess.create(james);

personAccess.findByName("James"); // { "name": "James", "age": 30 }
```

## Installation
Install with [maven](https://search.maven.org/artifact/com.jackhallam/weightless-orm/0.1.0-beta/jar):
```xml
<dependency>
  <groupId>com.jackhallam</groupId>
  <artifactId>weightless-orm</artifactId>
  <version>0.1.1-beta</version>
</dependency>
```

## Is Weightless for Me?
Is Weightless a good fit for your project? Weightless is designed to help projects get connected to a database as quickly as possible. Weightless is highly opinionated; it gives developers fewer customization options, and in return it is extremely easy to use.

That being said, Weightless does not lock you in. Off-boarding when your project is mature enough to need a highly customized solution is very simple. You can still use the same database, and you can move one database table or collection away from Weightless' control at a time as needed.

## Going Further
Visit [weightlessorm.com](https://weightlessorm.com) for a more information with live examples.
