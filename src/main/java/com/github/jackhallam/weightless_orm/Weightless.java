package com.github.jackhallam.weightless_orm;

import dev.morphia.Datastore;
import dev.morphia.Key;
import dev.morphia.query.FieldEnd;
import dev.morphia.query.Query;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Weightless {

  private Map<String, Object> myMap;
  private Datastore datastore;

  public Weightless(Datastore datastore) {
    try {
      this.datastore = datastore;
      myMap = new HashMap<>();

      Reflections reflections = new Reflections();
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
        myMap.put(clazz.getName(), o);
      }
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException();
    }
  }

  public <T> T getDal(Class<T> clazz) {
    return (T) myMap.get(clazz.getName());
  }

  public class GeneralInterceptor {
    @RuntimeType
    public Object intercept(@AllArguments Object[] allArguments, @Origin Method method) throws ClassNotFoundException, NoSuchMethodException {
      // intercept any method of any signature

      Annotation[] annotations = method.getDeclaredAnnotations();
      for (Annotation annotation : annotations) {
        if (annotation.annotationType().equals(Find.class)) {
          if (isMultiPartFn(annotations, method, allArguments)) {
            return handleMultiPartFn(annotations, method, allArguments);
          }
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
     * Perform filter aggregation by combining two functions in a particular way.
     */
    private Object handleMultiPartFn(Annotation[] annotations, Method method, Object[] allArguments) throws NoSuchMethodException, ClassNotFoundException {
      Set<String> multiPartFns = new HashSet<>();
      multiPartFns.add(And.class.getName());
      multiPartFns.add(Or.class.getName());
      Annotation annotation = null;

      for (int i = 0; i < annotations.length; i++) {
        if (multiPartFns.contains(annotations[i].annotationType().getName())) {
          annotation = annotations[i];
          break;
        }
      }
      if (annotation == null) {
        throw new RuntimeException("NO MULTI PART FN");
      }

      List<Object> childFnOutputs = new ArrayList<>();

      // Loop through 0 then 1 for the child fnNum (fn1, fn2)
      for (int childFnNum = 0; childFnNum <= 1; childFnNum++) {
        String fnName = getMultiPartAnnotationArg(annotation, childFnNum);

        Map<Integer, Class<?>> functionPassThruClassesForPosition = new HashMap<>();
        Map<Integer, Object> functionPassThruObjectsForPosition = new HashMap<>();

        // Parameters of the parent
        Parameter[] parameters = method.getParameters();
        for (int parentParamNum = 0; parentParamNum < parameters.length; parentParamNum++) {
          Parameter parentParameter = parameters[parentParamNum];
          Annotation[] parentParameterAnnotations = parentParameter.getAnnotations();
          for (int parentParamAnnotationNum = 0; parentParamAnnotationNum < parentParameterAnnotations.length; parentParamAnnotationNum++) {
            Annotation parentParamAnnotation = parentParameterAnnotations[parentParamAnnotationNum];
            if (!(parentParamAnnotation.annotationType().equals(PassTo.class) || parentParamAnnotation.annotationType().equals(PassTos.class))) {
              throw new RuntimeException("ONLY PASS TO ANNOTATION ALLOWED HERE");
            }

            // Multiple PassTo annotation cause a PassTos annotation
            List<PassTo> parentParamPassToAnnotations = new ArrayList<>();
            if (parentParamAnnotation.annotationType().equals(PassTos.class)) {
              PassTos parentParamPassTosAnnotation = (PassTos) parentParamAnnotation;
              parentParamPassToAnnotations.addAll(Arrays.asList(parentParamPassTosAnnotation.value()));
            } else {
              PassTo parentParamPassToAnnotation = (PassTo) parentParamAnnotation;
              parentParamPassToAnnotations.add(parentParamPassToAnnotation);
            }

            for (int parentPassToAnnotationNum = 0; parentPassToAnnotationNum < parentParamPassToAnnotations.size(); parentPassToAnnotationNum++) {
              // We only care about the pass to for fnName
              if (!parentParamPassToAnnotations.get(parentPassToAnnotationNum).fn().equals(fnName)) {
                continue;
              }
              functionPassThruClassesForPosition.put(parentParamPassToAnnotations.get(parentPassToAnnotationNum).paramNum(), parentParameter.getType());
              functionPassThruObjectsForPosition.put(parentParamPassToAnnotations.get(parentPassToAnnotationNum).paramNum(), allArguments[parentParamNum]);
            }
          }
        }

        // Build up a call to the child param
        int childParamCounter = 0;
        List<Object> objects = new ArrayList<>();
        List<Class<?>> classes = new ArrayList<>();
        while (functionPassThruObjectsForPosition.containsKey(childParamCounter)) {
          objects.add(functionPassThruObjectsForPosition.get(childParamCounter));
          classes.add(functionPassThruClassesForPosition.get(childParamCounter));
          childParamCounter++;
        }
        childFnOutputs.add(callCorrectChildFn(method.getDeclaringClass(), fnName, classes, objects));
      }

      return aggregateWithCorrectMultiPartFn(annotation, childFnOutputs.get(0), childFnOutputs.get(1));
    }

    /**
     * Calls the correct child method for the args, returns whatever that class does
     */
    private Object callCorrectChildFn(Class<?> declaringClass, String fnName, List<Class<?>> allArgumentsClasses, List<Object> allArguments) throws NoSuchMethodException, ClassNotFoundException {
      Class<?>[] allArgumentsClassesArray = new Class[allArgumentsClasses.size()];
      for (int i = 0; i < allArgumentsClasses.size(); i++) {
        allArgumentsClassesArray[i] = allArgumentsClasses.get(i);
      }

      // find correct method to call
      Method method = declaringClass.getMethod(fnName, allArgumentsClassesArray);

      Object[] allArgumentsArray = new Object[allArguments.size()];
      for (int i = 0; i < allArguments.size(); i++) {
        allArgumentsArray[i] = allArguments.get(i);
      }

      return intercept(allArgumentsArray, method);
    }

    private Object aggregateWithCorrectMultiPartFn(Annotation annotation, Object o1, Object o2) {
      Map<String, BiFunction<Object, Object, Object>> multiPartFns = new HashMap<>();
      multiPartFns.put(And.class.getName(), this::aggregateAndFn);
      multiPartFns.put(Or.class.getName(), this::aggregateOrFn);
      return multiPartFns.get(annotation.annotationType().getName()).apply(o1, o2);
    }

    private Object aggregateOrFn(Object o1, Object o2) {
      try {
        List<Object> fn1Results = (List<Object>) o1;
        List<Object> fn2Results = (List<Object>) o2;
        List<Object> combined = new ArrayList<>();
        for (Object fn1Obj : fn1Results) {
          boolean alreadyInCombined = false;
          for (Object o : combined) {
            if (databaseItemsEqual(fn1Obj, o)) {
              alreadyInCombined = true;
              break;
            }
          }
          if (!alreadyInCombined) {
            combined.add(fn1Obj);
          }
        }
        for (Object fn2Obj : fn2Results) {
          boolean alreadyInCombined = false;
          for (Object o : combined) {
            if (databaseItemsEqual(fn2Obj, o)) {
              alreadyInCombined = true;
              break;
            }
          }
          if (!alreadyInCombined) {
            combined.add(fn2Obj);
          }
        }
        return combined;
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

    private Object aggregateAndFn(Object o1, Object o2) {
      try {
        List<Object> fn1Results = (List<Object>) o1;
        List<Object> fn2Results = (List<Object>) o2;
        List<Object> combined = new ArrayList<>();
        for (Object fn1Obj : fn1Results) {
          for (Object fn2Obj : fn2Results) {
            if (databaseItemsEqual(fn1Obj, fn2Obj)) {
              boolean alreadyInCombined = false;
              for (Object o : combined) {
                if (databaseItemsEqual(fn1Obj, o)) {
                  alreadyInCombined = true;
                  break;
                }
              }
              if (!alreadyInCombined) {
                combined.add(fn1Obj);
              }
            }
          }
        }
        return combined;
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private boolean databaseItemsEqual(Object o1, Object o2) throws IllegalAccessException {
    java.lang.reflect.Field idFieldO1 = null;
    for (java.lang.reflect.Field field : o1.getClass().getDeclaredFields()) {
      if (field.getAnnotation(dev.morphia.annotations.Id.class) != null) {
        idFieldO1 = field;
        break;
      }
    }
    if (idFieldO1 == null) {
      throw new RuntimeException("NO ID ON " + o1.getClass().getName());
    }

    java.lang.reflect.Field idFieldO2 = null;
    for (java.lang.reflect.Field field : o2.getClass().getDeclaredFields()) {
      if (field.getAnnotation(dev.morphia.annotations.Id.class) != null) {
        idFieldO2 = field;
        break;
      }
    }
    if (idFieldO2 == null) {
      throw new RuntimeException("NO ID ON " + o1.getClass().getName());
    }

    boolean isIdField01Accessible = idFieldO1.isAccessible();
    idFieldO1.setAccessible(true);
    Object idO1Val = idFieldO1.get(o1);
    idFieldO1.setAccessible(isIdField01Accessible);

    boolean isIdField02Accessible = idFieldO2.isAccessible();
    idFieldO2.setAccessible(true);
    Object idO2Val = idFieldO2.get(o2);
    idFieldO2.setAccessible(isIdField02Accessible);

    return idO1Val.equals(idO2Val);
  }

  /**
   * Finds the function names referenced in the method level
   */
  private String getMultiPartAnnotationArg(Annotation annotation, int i) {
    Map<String, Supplier<String>> multiPartFns = new HashMap<>();
    multiPartFns.put(And.class.getName(), () -> i == 0 ? ((And) annotation).fn1() : ((And) annotation).fn2());
    multiPartFns.put(Or.class.getName(), () -> i == 0 ? ((Or) annotation).fn1() : ((Or) annotation).fn2());
    return multiPartFns.get(annotation.annotationType().getName()).get();
  }

  private boolean isMultiPartFn(Annotation[] annotations, Method method, Object[] allArguments) {
    Set<String> multiPartFns = new HashSet<>();
    multiPartFns.add(And.class.getName());
    multiPartFns.add(Or.class.getName());

    for (int i = 0; i < annotations.length; i++) {
      if (multiPartFns.contains(annotations[i].annotationType().getName())) {
        return true;
      }
    }
    return false;
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
    for (int i = 0; i < method.getParameterCount(); i++) {
      Parameter parameter = method.getParameters()[i];
      FieldEnd<?> fieldEnd = null;
      Annotation[] annotations = parameter.getAnnotations();
      for (Annotation annotation : annotations) {
        if (annotation.annotationType().equals(Field.class)) {
          String fieldName = ((Field) annotation).value();
          fieldEnd = q.field(fieldName);
        }
        if (annotation.annotationType().equals(Equals.class)) {
          if (fieldEnd == null) {
            throw new RuntimeException("FIELD NOT PROVIDED");
          }
          fieldEnd.equal(allArguments[i]);
        }
        if (annotation.annotationType().equals(Lte.class)) {
          if (fieldEnd == null) {
            throw new RuntimeException("FIELD NOT PROVIDED");
          }
          fieldEnd.lessThanOrEq(allArguments[i]);
        }
        if (annotation.annotationType().equals(Gte.class)) {
          if (fieldEnd == null) {
            throw new RuntimeException("FIELD NOT PROVIDED");
          }
          fieldEnd.greaterThanOrEq(allArguments[i]);
        }
        if (annotation.annotationType().equals(HasAnyOf.class)) {
          if (fieldEnd == null) {
            throw new RuntimeException("FIELD NOT PROVIDED");
          }
          fieldEnd.greaterThanOrEq(allArguments[i]);
        }
      }
    }
  }
}
