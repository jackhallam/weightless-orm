<p align="center">
<img width="150" height="150" src="weightlessicon.png" title="windy by K available at https://thenounproject.com/k4dezign/collection/weather/?i=455834. CC 3.0 BY licensed (http://creativecommons.org/licenses/by/3.0/)">
</p>

<h1 align="center">Weightless ORM</h1>

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
Weightless is a [object-relational mapper](https://en.wikipedia.org/wiki/Object%E2%80%93relational_mapping) library for Java. Save and query Java objects in a database in just a few lines of code. Weightless is still in development, and currently supports MongoDB and a generic in-memory store for testing.

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
  <version>d35b2241f9</version>
</dependency>
```

### Usage
Assume we have an object we want to read and write to a database
```java
class MyObject {
  String foo;
  int bar;
}
```
Define an interface that describes how to access our objects
```java
interface MyObjectAccess {
  @Find
  TestObject find(@Field("foo") @Equals String foo);

  @Find
  @Sort(onField = "bar")
  List<TestObject> findAllSorted();

  @Create
  TestObject create(TestObject testObject);
}
```
The Weightless library will fill in the access interface at runtime
```java
Weightless weightless = WeightlessORMBuilder.inMemory().build(); // In-Memory database for local testing
MyObjectAccess access = weightless.get(MyObjectAccess.class);
```
```java
access.create(new MyObject("hello", 99));
access.find("hello"); // { "foo": "hello", "bar": 99 }

access.create(new MyObject("world", 50));
access.findAllSorted(); // [{ "foo": "world", "bar": 50 }, { "foo": "hello", "bar": 99 }]
```
