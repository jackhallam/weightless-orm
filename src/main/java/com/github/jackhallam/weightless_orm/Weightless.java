package com.github.jackhallam.weightless_orm;

import com.github.jackhallam.weightless_orm.annotations.*;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Equals;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Gte;
import com.github.jackhallam.weightless_orm.annotations.field_filters.HasAnyOf;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Lte;
import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Key;
import dev.morphia.Morphia;
import dev.morphia.query.FieldEnd;
import dev.morphia.query.Query;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;
import org.reflections.Reflections;

import java.io.Closeable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class Weightless implements Closeable {

  private final MongoClient mongoClient;
  private final Datastore datastore;
  private final Map<String, Object> interceptedClassNameToObject;

  public Weightless(MongoClient mongoClient, String databaseName) {
    try {
      this.mongoClient = mongoClient;
      Morphia morphia = new Morphia();
      morphia.mapPackage("");
      datastore = morphia.createDatastore(mongoClient, databaseName);

      interceptedClassNameToObject = new HashMap<>();

      Reflections reflections = new Reflections("");

      Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Dal.class);
      for (Class<?> clazz : annotated) {
        Class<?> dynamicType = new ByteBuddy()
          .subclass(clazz)
          .method(ElementMatchers.any())
          .intercept(MethodDelegation.to(new GeneralInterceptor()))
          .make()
          .load(getClass().getClassLoader())
          .getLoaded();
        Object o = dynamicType.newInstance();
        interceptedClassNameToObject.put(clazz.getName(), o);
      }
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException();
    }
  }

  public <T> T get(Class<T> clazz) {
    return (T) interceptedClassNameToObject.get(clazz.getName());
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
    if (mongoClient != null) {
      mongoClient.close();
    }
  }

  public class GeneralInterceptor {
    @RuntimeType
    public Object intercept(@AllArguments Object[] allArguments, @Origin Method method) throws ClassNotFoundException, NoSuchMethodException {
      // intercept any method of any signature

      Annotation[] annotations = method.getDeclaredAnnotations();
      for (Annotation annotation : annotations) {
        if (annotation.annotationType().equals(Find.class)) {
          Class<?> clazz = Class.forName(getInnerTypeIfPresent(method.getGenericReturnType()).getTypeName());
          Query<?> q = datastore.find(clazz);
          addFilters(q, method, allArguments);
          addSorts(q, annotations);
          return returnCorrectWrapper(q, method);
        }
        if (annotation.annotationType().equals(Add.class) || annotation.annotationType().equals(Update.class)) {
          Class<?> clazz = method.getParameterTypes()[0];
          Key<?> key = datastore.save(clazz.cast(allArguments[0]));
          java.lang.reflect.Field idField = null;
          for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(dev.morphia.annotations.Id.class) != null) {
              idField = field;
              break;
            }
          }
          if (idField == null) {
            throw new RuntimeException("NO ID ON " + clazz.getName());
          }
          Query<?> q = datastore.find(clazz).field(idField.getName()).equal(key.getId());
          return returnCorrectWrapper(q, method);
        }
      }
      throw new RuntimeException("NOT INTERCEPTED " + method.getName());
    }

    /**
     * Looks through annotations and applies sorts in the correct order
     */
    private void addSorts(Query<?> q, Annotation[] annotations) {
      List<Sort> sortAnnotations = new ArrayList<>();
      for (Annotation methodLevelAnnotation : annotations) {
        if (methodLevelAnnotation.annotationType().equals(Sorts.class)) {
          Sorts sortsAnnotation = (Sorts) methodLevelAnnotation;
          sortAnnotations.addAll(Arrays.asList(sortsAnnotation.value()));
        } else if (methodLevelAnnotation.annotationType().equals(Sort.class)) {
          Sort sortAnnotation = (Sort) methodLevelAnnotation;
          sortAnnotations.add(sortAnnotation);
        }
      }

      String sortString = sortAnnotations.stream().map(sortAnnotation -> {
        String by = sortAnnotation.onField();
        boolean isAscending = sortAnnotation.direction() == Sort.Direction.ASCENDING;
        return (isAscending ? "" : "-") + by;
      }).collect(Collectors.joining(","));
      if (!sortString.isEmpty()) {
        q.order(sortString);
      }
    }

    /**
     * Finds the correct wrapper (List, optional, pojo) and returns the q conforming to that
     */
    private Object returnCorrectWrapper(Query<?> q, Method method) {
      if (method.getReturnType().equals(List.class)) {
        return q.find().toList();
      }
      if (method.getReturnType().equals(Optional.class)) {
        return Optional.ofNullable(q.iterator().tryNext());
      }
      return q.iterator().tryNext();
    }

    private Type getInnerTypeIfPresent(Type outerType) {
      Type[] types = ((ParameterizedType) outerType).getActualTypeArguments();
      if (types.length == 0) {
        return outerType;
      }
      return Arrays.stream(((ParameterizedType) outerType).getActualTypeArguments()).findFirst().get();
    }

    /**
     * Filters the query based on the annotation filters provided
     */
    private void addFilters(Query<?> q, Method method, Object[] allArguments) {

      Map<Class<? extends Annotation>, Filter> filterMap = getFiltersMap();

      for (int i = 0; i < method.getParameterCount(); i++) {
        Parameter parameter = method.getParameters()[i];
        FieldEnd<?> fieldEnd = null;
        Annotation[] annotations = parameter.getAnnotations();
        for (Annotation annotation : annotations) {
          if (annotation.annotationType().equals(Field.class)) {
            String fieldName = ((Field) annotation).value();
            fieldEnd = q.field(fieldName);
          } else {
            if (fieldEnd == null) {
              throw new RuntimeException("FIELD NOT PROVIDED");
            }
            if (!filterMap.containsKey(annotation.annotationType())) {
              throw new RuntimeException("Unknown filter " + annotation.annotationType());
            }
            filterMap.get(annotation.annotationType()).filter(fieldEnd, allArguments[i]);
          }
        }
      }
    }

    private Map<Class<? extends Annotation>, Filter> getFiltersMap() {
      Map<Class<? extends Annotation>, Filter> filtersMap = new HashMap<>();
      filtersMap.put(Equals.class, (fieldEnd, fieldValue) -> fieldEnd.equal(fieldValue));
      filtersMap.put(Lte.class, (fieldEnd, fieldValue) -> fieldEnd.lessThanOrEq(fieldValue));
      filtersMap.put(Gte.class, (fieldEnd, fieldValue) -> fieldEnd.greaterThanOrEq(fieldValue));
      filtersMap.put(HasAnyOf.class, (fieldEnd, fieldValue) -> fieldEnd.hasAnyOf((Iterable<?>) fieldValue));
      return filtersMap;
    }
  }

  interface Filter {
    void filter(FieldEnd<?> fieldEnd, Object fieldValue);
  }
}
