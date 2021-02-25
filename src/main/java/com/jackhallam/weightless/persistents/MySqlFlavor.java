package com.jackhallam.weightless.persistents;

import java.util.ArrayList;
import java.util.List;

public class MySqlFlavor implements SqlFlavor {

  @Override
  public String booleanAsSql() {
    return "BOOL";
  }

  @Override
  public String intAsSql() {
    return "INT";
  }

  @Override
  public String longAsSql() {
    return "BIGINT";
  }

  @Override
  public String doubleAsSql() {
    return "DOUBLE";
  }

  @Override
  public String stringAsSql() {
    return "LONGTEXT";
  }

  @Override
  public void contains(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks) {
    queryBuilder.append(fieldName).append(" LIKE BINARY (?) ");
    questionMarks.add("%" + value + "%");
  }

  @Override
  public void containsIgnoreCase(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks) {
    queryBuilder.append(fieldName).append(" LIKE (?) ");
    questionMarks.add("%" + value + "%");
  }

  @Override
  public void doesNotExist(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks) {
    queryBuilder.append(fieldName).append(" IS NULL ");
  }

  @Override
  public void endsWith(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks) {
    queryBuilder.append(fieldName).append(" LIKE BINARY (?) ");
    questionMarks.add("%" + value);
  }

  @Override
  public void endsWithIgnoreCase(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks) {
    queryBuilder.append(fieldName).append(" LIKE (?) ");
    questionMarks.add("%" + value);
  }

  @Override
  public void equals(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks) {
    queryBuilder.append(fieldName).append(" = (?) ");
    questionMarks.add(value);
  }

  @Override
  public void exists(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks) {
    queryBuilder.append(fieldName).append(" IS NOT NULL ");
  }

  @Override
  public void greaterThan(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks) {
    queryBuilder.append(fieldName).append(" > (?) ");
    questionMarks.add(value);
  }

  @Override
  public void greaterThanOrEqualTo(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks) {
    queryBuilder.append(fieldName).append(" >= (?) ");
    questionMarks.add(value);
  }

  @Override
  public void hasAnyOf(String fieldName, List<String> items, StringBuilder queryBuilder, List<String> questionMarks) {
    List<String> parts = new ArrayList<>();
    for (String item : items) {
      parts.add(fieldName + " = (?)");
      questionMarks.add(item);
    }
    queryBuilder.append("(").append(String.join(" OR ", parts)).append(")");
  }

  @Override
  public void hasNoneOf(String fieldName, List<String> items, StringBuilder queryBuilder, List<String> questionMarks) {
    List<String> parts = new ArrayList<>();
    for (String item : items) {
      parts.add(fieldName + " != (?)");
      questionMarks.add(item);
    }
    queryBuilder.append("(").append(String.join(" AND ", parts)).append(")");
  }

  @Override
  public void lessThan(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks) {
    queryBuilder.append(fieldName).append(" < (?) ");
    questionMarks.add(value);
  }

  @Override
  public void lessThanOrEqualTo(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks) {
    queryBuilder.append(fieldName).append(" <= (?) ");
    questionMarks.add(value);
  }

  @Override
  public void startsWith(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks) {
    queryBuilder.append(fieldName).append(" LIKE BINARY (?) ");
    questionMarks.add(value + "%");
  }

  @Override
  public void startsWithIgnoreCase(String fieldName, String value, StringBuilder queryBuilder, List<String> questionMarks) {
    queryBuilder.append(fieldName).append(" LIKE (?) ");
    questionMarks.add(value + "%");
  }
}
