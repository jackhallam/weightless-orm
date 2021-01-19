package com.github.jackhallam.weightless_orm.interceptors.handlers;

import com.github.jackhallam.weightless_orm.WeightlessORMException;
import com.github.jackhallam.weightless_orm.annotations.Field;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConditionHandler {

  private List<SubFilter<?>> subFilters;

  public ConditionHandler() {
    this.subFilters = new ArrayList<>();
  }

  public ConditionHandler(java.lang.reflect.Parameter[] parameters, Object[] allArguments) {
    this.subFilters = buildSubFilters(parameters, allArguments);
  }

  public Iterator<SubFilter<?>> getSubFiltersIterator() {
    return subFilters.iterator();
  }

  public List<SubFilter<?>> buildSubFilters(java.lang.reflect.Parameter[] parameters, Object[] allArguments) {
    List<SubFilter<?>> subFilters = new ArrayList<>();
    for (int i = 0; i < parameters.length; i++) {
      java.lang.reflect.Parameter parameter = parameters[i];
      Annotation[] annotations = parameter.getAnnotations();
      if (annotations.length == 0) {
        continue; // Skip if there are not any annotations (such as the @Update case)
      }
      if (annotations.length != 2) {
        throw new WeightlessORMException("Expected 2 annotations on the parameter but found " + annotations.length);
      }
      if (!annotations[0].annotationType().equals(Field.class)) {
        throw new WeightlessORMException("Expected " + Field.class + " as the first annotation on the parameter but found " + annotations[0].annotationType());
      }

      String fieldName = ((Field) annotations[0]).value();
      Object value = allArguments[i];

      SubFilter<? extends Annotation> subFilter = new SubFilter<>(fieldName, value, annotations[1]);
      subFilters.add(subFilter);
    }
    return subFilters;
  }

  public static class SubFilter<AnnotationType extends Annotation> {
    public String fieldName;
    public Object value;
    public AnnotationType filterTypeAnnotation;

    public SubFilter(String fieldName, Object value, AnnotationType filterTypeAnnotation) {
      this.fieldName = fieldName;
      this.value = value;
      this.filterTypeAnnotation = filterTypeAnnotation;
    }
  }
}
