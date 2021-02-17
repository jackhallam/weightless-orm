package com.jackhallam.weightless.persistents;

import java.util.List;

public interface SqlFlavor {

  String booleanAsSql();
  String intAsSql();
  String longAsSql();
  String doubleAsSql();
  String stringAsSql();

  void contains(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks);

  void containsIgnoreCase(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks);

  void doesNotExist(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks);

  void endsWith(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks);

  void endsWithIgnoreCase(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks);

  void equals(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks);

  void exists(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks);

  void greaterThan(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks);

  void greaterThanOrEqualTo(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks);

  void hasAnyOf(String fieldName, List<String> items, StringBuilder queryBuilder, List<String> questionMarks);

  void hasNoneOf(String fieldName, List<String> items, StringBuilder queryBuilder, List<String> questionMarks);

  void lessThan(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks);

  void lessThanOrEqualTo(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks);

  void startsWith(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks);

  void startsWithIgnoreCase(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks);
}
