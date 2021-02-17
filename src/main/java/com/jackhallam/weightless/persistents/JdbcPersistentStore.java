package com.jackhallam.weightless.persistents;

import com.jackhallam.weightless.WeightlessException;
import com.jackhallam.weightless.annotations.Sort;
import com.jackhallam.weightless.annotations.field_filters.Contains;
import com.jackhallam.weightless.annotations.field_filters.ContainsIgnoreCase;
import com.jackhallam.weightless.annotations.field_filters.DoesNotExist;
import com.jackhallam.weightless.annotations.field_filters.EndsWith;
import com.jackhallam.weightless.annotations.field_filters.EndsWithIgnoreCase;
import com.jackhallam.weightless.annotations.field_filters.Equals;
import com.jackhallam.weightless.annotations.field_filters.Exists;
import com.jackhallam.weightless.annotations.field_filters.GreaterThan;
import com.jackhallam.weightless.annotations.field_filters.GreaterThanOrEqualTo;
import com.jackhallam.weightless.annotations.field_filters.HasAnyOf;
import com.jackhallam.weightless.annotations.field_filters.HasNoneOf;
import com.jackhallam.weightless.annotations.field_filters.LessThan;
import com.jackhallam.weightless.annotations.field_filters.LessThanOrEqualTo;
import com.jackhallam.weightless.annotations.field_filters.StartsWith;
import com.jackhallam.weightless.annotations.field_filters.StartsWithIgnoreCase;
import com.jackhallam.weightless.interceptors.handlers.ConditionHandler;
import com.jackhallam.weightless.interceptors.handlers.SortHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JdbcPersistentStore implements PersistentStore {

  private final Connection connection;
  private final Set<String> completedTablesClassNames = new HashSet<>();
  private final SqlFlavor sqlFlavor;

  public JdbcPersistentStore(Connection connection, SqlFlavor sqlFlavor) {
    this.connection = connection;
    this.sqlFlavor = sqlFlavor;
  }

  @Override
  public <T> Iterable<T> create(Iterable<T> tIterable) {

    List<T> inserted = new ArrayList<>();

    tIterable.forEach(t -> {
      try {
        Class<T> clazz = (Class<T>) t.getClass();
        String classNameUpper = clazz.getSimpleName().toUpperCase();

        completeTable(clazz);

        // INSERT INTO table_name (column1, column2, column3, ...) VALUES (?, ?, ?, ...)
        StringBuilder insertStringBuilder = new StringBuilder("INSERT INTO ");
        insertStringBuilder.append(classNameUpper);
        insertStringBuilder.append(" (");
        List<String> columnsUpper = new ArrayList<>();
        List<java.lang.reflect.Field> fieldsColumns = new ArrayList<>();
        java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
          String fieldNameUpper = field.getName().toUpperCase();
          boolean isAccessible = field.isAccessible();
          field.setAccessible(true);
          if (field.get(t) == null) {
            continue;
          }
          columnsUpper.add(fieldNameUpper);
          fieldsColumns.add(field);
          field.setAccessible(isAccessible);
        }
        insertStringBuilder.append(String.join(", ", columnsUpper));
        insertStringBuilder.append(") VALUES (");

        insertStringBuilder.append(columnsUpper.stream().map(item -> "?").collect(Collectors.joining(", ")));
        insertStringBuilder.append(")");

        try (PreparedStatement insertStatement = connection.prepareStatement(insertStringBuilder.toString())) {

          for (int i = 1; i <= fieldsColumns.size(); i++) {
            java.lang.reflect.Field field = fieldsColumns.get(i - 1);
            boolean isAccessible = field.isAccessible();
            field.setAccessible(true);
            insertStatement.setString(i, field.get(t).toString());
            field.setAccessible(isAccessible);
          }

          insertStatement.execute();

          if (insertStatement.getUpdateCount() == 0) {
            throw new WeightlessException("Not created");
          }

          inserted.add(t);
        }

      } catch (SQLException | IllegalAccessException e) {
        throw new WeightlessException(e);
      }
    });

    return inserted;
  }

  @Override
  public <T> Iterable<T> find(Class<T> clazz, ConditionHandler conditionHandler, SortHandler<T> sortHandler) {
    try {
      completeTable(clazz);

      String classNameUpper = clazz.getSimpleName().toUpperCase();

      List<T> output = new ArrayList<>();

      List<String> columnsUpper = new ArrayList<>();
      List<java.lang.reflect.Field> fieldsColumns = new ArrayList<>();
      java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
      for (java.lang.reflect.Field field : fields) {
        String fieldNameUpper = field.getName().toUpperCase();
        boolean isAccessible = field.isAccessible();
        field.setAccessible(true);
        columnsUpper.add(fieldNameUpper);
        fieldsColumns.add(field);
        field.setAccessible(isAccessible);
      }

      // SELECT A, B, C FROM TABLENAME
      StringBuilder queryStringBuilder = new StringBuilder("SELECT ");
      queryStringBuilder.append(String.join(", ", columnsUpper));
      queryStringBuilder.append(" FROM ");
      queryStringBuilder.append(classNameUpper);

      List<String> questionMarks = new ArrayList<>();
      String whereClause = whereBuilder(conditionHandler.getSubFiltersIterator(), questionMarks);
      queryStringBuilder.append(whereClause);

      // ORDER BY column1 ASC|DESC, column2 ASC|DESC, ... ;
      if (sortHandler.getSortsIterator().hasNext()) {

        queryStringBuilder.append(" ORDER BY ");

        List<String> sorts = new ArrayList<>();
        sortHandler.getSortsIterator().forEachRemaining(sortContainer -> {
          StringBuilder sortBuilder = new StringBuilder();
          sortBuilder.append(sortContainer.fieldName.toUpperCase());

          if (sortContainer.direction.equals(Sort.Direction.ASCENDING)) {
            sortBuilder.append(" ASC ");
          } else {
            sortBuilder.append(" DESC ");
          }
          sorts.add(sortBuilder.toString());
        });

        queryStringBuilder.append(String.join(", ", sorts));
      }

      try (PreparedStatement selectStatement = connection.prepareStatement(queryStringBuilder.toString())) {

        for (int i = 1; i <= questionMarks.size(); i++) {
          String questionMarkValue = questionMarks.get(i - 1);
          selectStatement.setString(i, questionMarkValue);
        }

        ResultSet resultSet = selectStatement.executeQuery();
        while (resultSet.next()) {
          T t = clazz.newInstance();
          for (int i = 0; i < fieldsColumns.size(); i++) {
            Object o = resultSet.getObject(columnsUpper.get(i));
            boolean isAccessible = fieldsColumns.get(i).isAccessible();
            fieldsColumns.get(i).setAccessible(true);
            fieldsColumns.get(i).set(t, o);
            fieldsColumns.get(i).setAccessible(isAccessible);
          }
          output.add(t);
        }
      }
      return output;
    } catch (SQLException | InstantiationException | IllegalAccessException e) {
      throw new WeightlessException(e);
    }
  }

  @Override
  public <T> Iterable<T> update(Iterable<T> tIterable, ConditionHandler conditionHandler) {

    Iterator<T> tIterator = tIterable.iterator();
    if (!tIterator.hasNext()) {
      throw new WeightlessException("No object to use to update.");
    }

    T t = tIterator.next();

    if (tIterator.hasNext()) {
      throw new WeightlessException("Expected only one object to update but found more than one.");
    }

    Class<T> clazz = (Class<T>) t.getClass();

    Iterable<T> deletedIterable = this.delete(clazz, conditionHandler);
    Iterator<T> deletedIterator = deletedIterable.iterator();
    if (!deletedIterator.hasNext()) {
      return Collections.emptyList(); // We did not find an item to update
    }

    return create(Collections.singletonList(t)); // Convert that single object back to iterable and save it
  }

  @Override
  public <T> Iterable<T> delete(Class<T> clazz, ConditionHandler conditionHandler) {
    try {
      completeTable(clazz);

      String classNameUpper = clazz.getSimpleName().toUpperCase();

      Iterable<T> foundIterable = find(clazz, conditionHandler, new SortHandler<>());
      List<T> found = StreamSupport.stream(foundIterable.spliterator(), false).collect(Collectors.toList());

      List<String> columnsUpper = new ArrayList<>();
      List<java.lang.reflect.Field> fieldsColumns = new ArrayList<>();
      java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
      for (java.lang.reflect.Field field : fields) {
        String fieldNameUpper = field.getName().toUpperCase();
        boolean isAccessible = field.isAccessible();
        field.setAccessible(true);
        columnsUpper.add(fieldNameUpper);
        fieldsColumns.add(field);
        field.setAccessible(isAccessible);
      }

      // SELECT A, B, C FROM TABLENAME
      StringBuilder queryStringBuilder = new StringBuilder("Delete FROM ");
      queryStringBuilder.append(classNameUpper);

      List<String> questionMarks = new ArrayList<>();
      String whereClause = whereBuilder(conditionHandler.getSubFiltersIterator(), questionMarks);
      queryStringBuilder.append(whereClause);

      try (PreparedStatement deleteStatement = connection.prepareStatement(queryStringBuilder.toString())) {

        for (int i = 1; i <= questionMarks.size(); i++) {
          String questionMarkValue = questionMarks.get(i - 1);
          deleteStatement.setString(i, questionMarkValue);
        }

        int out = deleteStatement.executeUpdate();
        if (out != found.size()) {
          return new ArrayList<>();
        }
      }
      return found;
    } catch (SQLException e) {
      throw new WeightlessException(e);
    }
  }

  public String whereBuilder(Iterator<ConditionHandler.SubFilter<?>> subFilterIterator, List<String> questionMarks) {
    StringBuilder queryStringBuilder = new StringBuilder();

    if (subFilterIterator.hasNext()) {
      queryStringBuilder.append(" WHERE ");
    }
    AtomicBoolean isFirst = new AtomicBoolean(true);

    subFilterIterator.forEachRemaining(subFilter -> {
      String fieldNameUpper = subFilter.fieldName.toUpperCase();
      String value = subFilter.value == null ? null : subFilter.value.toString();

      if (isFirst.get()) {
        isFirst.set(false);
      } else {
        queryStringBuilder.append(" AND ");
      }

      if (subFilter.filterTypeAnnotation.annotationType().equals(Contains.class)) {
        sqlFlavor.contains(fieldNameUpper, value, queryStringBuilder, questionMarks);
      }

      if (subFilter.filterTypeAnnotation.annotationType().equals(ContainsIgnoreCase.class)) {
        sqlFlavor.containsIgnoreCase(fieldNameUpper, value, queryStringBuilder, questionMarks);
      }

      if (subFilter.filterTypeAnnotation.annotationType().equals(DoesNotExist.class)) {
        sqlFlavor.doesNotExist(fieldNameUpper, value, queryStringBuilder, questionMarks);
      }

      if (subFilter.filterTypeAnnotation.annotationType().equals(EndsWith.class)) {
        sqlFlavor.endsWith(fieldNameUpper, value, queryStringBuilder, questionMarks);
      }

      if (subFilter.filterTypeAnnotation.annotationType().equals(EndsWithIgnoreCase.class)) {
        sqlFlavor.endsWithIgnoreCase(fieldNameUpper, value, queryStringBuilder, questionMarks);
      }

      if (subFilter.filterTypeAnnotation.annotationType().equals(Equals.class)) {
        sqlFlavor.equals(fieldNameUpper, value, queryStringBuilder, questionMarks);
      }

      if (subFilter.filterTypeAnnotation.annotationType().equals(Exists.class)) {
        sqlFlavor.exists(fieldNameUpper, value, queryStringBuilder, questionMarks);
      }

      if (subFilter.filterTypeAnnotation.annotationType().equals(GreaterThan.class)) {
        sqlFlavor.greaterThan(fieldNameUpper, value, queryStringBuilder, questionMarks);
      }

      if (subFilter.filterTypeAnnotation.annotationType().equals(GreaterThanOrEqualTo.class)) {
        sqlFlavor.greaterThanOrEqualTo(fieldNameUpper, value, queryStringBuilder, questionMarks);
      }

      if (subFilter.filterTypeAnnotation.annotationType().equals(HasAnyOf.class)) {
        sqlFlavor.hasAnyOf(fieldNameUpper, StreamSupport.stream(((Iterable<?>) subFilter.value).spliterator(), false).map(Object::toString).collect(Collectors.toList()), queryStringBuilder, questionMarks);
      }

      if (subFilter.filterTypeAnnotation.annotationType().equals(HasNoneOf.class)) {
        sqlFlavor.hasNoneOf(fieldNameUpper, StreamSupport.stream(((Iterable<?>) subFilter.value).spliterator(), false).map(Object::toString).collect(Collectors.toList()), queryStringBuilder, questionMarks);
      }

      if (subFilter.filterTypeAnnotation.annotationType().equals(LessThan.class)) {
        sqlFlavor.lessThan(fieldNameUpper, value, queryStringBuilder, questionMarks);
      }

      if (subFilter.filterTypeAnnotation.annotationType().equals(LessThanOrEqualTo.class)) {
        sqlFlavor.lessThanOrEqualTo(fieldNameUpper, value, queryStringBuilder, questionMarks);
      }

      if (subFilter.filterTypeAnnotation.annotationType().equals(StartsWith.class)) {
        sqlFlavor.startsWith(fieldNameUpper, value, queryStringBuilder, questionMarks);
      }

      if (subFilter.filterTypeAnnotation.annotationType().equals(StartsWithIgnoreCase.class)) {
        sqlFlavor.startsWithIgnoreCase(fieldNameUpper, value, queryStringBuilder, questionMarks);
      }
    });

    return queryStringBuilder.toString();
  }

  private <T> void completeTable(Class<T> clazz) throws SQLException {
    if (completedTablesClassNames.contains(clazz.getName())) {
      return;
    }

    if (!tableExists(clazz)) {
      createTable(clazz);
      completedTablesClassNames.add(clazz.getName());
      return;
    }

    try (Statement findColumnsStatement = connection.createStatement()) {
      String classNameUpper = clazz.getSimpleName().toUpperCase();
      ResultSet findColumnsResultSet = findColumnsStatement.executeQuery("select * from " + classNameUpper);
      ResultSetMetaData findColumnsResultSetMetaData = findColumnsResultSet.getMetaData();
      Set<String> columnNamesUpper = new HashSet<>();
      for (int i = 1; i <= findColumnsResultSetMetaData.getColumnCount(); i++) {
        columnNamesUpper.add(findColumnsResultSetMetaData.getColumnName(i).toUpperCase());
      }
      java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
      for (java.lang.reflect.Field field : fields) {
        String fieldNameUpper = field.getName().toUpperCase();
        if (!columnNamesUpper.contains(fieldNameUpper)) {
          Optional<String> sqlTypeOptional = javaTypeToSQLType(field.getType());
          if (!sqlTypeOptional.isPresent()) {
            continue;
          }
          String sqlType = sqlTypeOptional.get();
          try (Statement addFieldStatement = connection.createStatement()) {
            addFieldStatement.executeUpdate("ALTER TABLE " + classNameUpper + " ADD " + fieldNameUpper + " " + sqlType);
          }
        }
      }
    }
  }

  private <T> boolean tableExists(Class<T> clazz) throws SQLException {
    String classNameUpper = clazz.getSimpleName().toUpperCase();
    return connection.getMetaData().getTables(null, null, classNameUpper, null).next();
  }

  private <T> void createTable(Class<T> clazz) throws SQLException {
    String classNameUpper = clazz.getSimpleName().toUpperCase();
    // In the form "create table TABLENAME (field1 INT, field2 VARCHAR)"
    StringBuilder sqlBuilder = new StringBuilder("CREATE TABLE ");
    sqlBuilder.append(classNameUpper);
    sqlBuilder.append(" (");
    List<String> fieldNamesUpperAndSQLType = new ArrayList<>();
    java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
    for (java.lang.reflect.Field field : fields) {
      Optional<String> sqlTypeOptional = javaTypeToSQLType(field.getType());
      if (!sqlTypeOptional.isPresent()) {
        continue;
      }
      String sqlType = sqlTypeOptional.get();
      fieldNamesUpperAndSQLType.add(field.getName().toUpperCase() + " " + sqlType);
    }
    sqlBuilder.append(String.join(", ", fieldNamesUpperAndSQLType));
    sqlBuilder.append(")");
    String createTableSQL = sqlBuilder.toString();
    try (Statement createTableStatement = connection.createStatement()) {
      createTableStatement.execute(createTableSQL);
    }
  }

  private <S> Optional<String> javaTypeToSQLType(Class<S> clazz) {
    HashMap<String, String> javaTypeToSQLType = new HashMap<>();
    javaTypeToSQLType.put(boolean.class.getTypeName(), sqlFlavor.booleanAsSql());
    javaTypeToSQLType.put(int.class.getTypeName(), sqlFlavor.intAsSql());
    javaTypeToSQLType.put(long.class.getTypeName(), sqlFlavor.longAsSql());
    javaTypeToSQLType.put(double.class.getTypeName(), sqlFlavor.doubleAsSql());
    javaTypeToSQLType.put(String.class.getTypeName(), sqlFlavor.stringAsSql());

    if (javaTypeToSQLType.containsKey(clazz.getTypeName())) {
      return Optional.ofNullable(javaTypeToSQLType.get(clazz.getTypeName()));
    }
    return Optional.empty();
  }

  /**
   * Closes this stream and releases any system resources associated
   * with it. If the stream is already closed then invoking this
   * method has no effect.
   *
   * <p> As noted in {@link AutoCloseable#close()}, cases where the
   * close may fail require careful attention. It is strongly advised
   * to relinquish the underlying resources and to internally
   * <em>mark</em> the {@code Closeable} as closed, prior to throwing
   * the {@code IOException}.
   *
   * @throws IOException if an I/O error occurs
   */
  @Override
  public void close() throws IOException {
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        throw new IOException(e);
      }
    }
  }
}
