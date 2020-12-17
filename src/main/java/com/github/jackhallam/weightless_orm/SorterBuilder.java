package com.github.jackhallam.weightless_orm;

import com.github.jackhallam.weightless_orm.annotations.Sort;
import com.github.jackhallam.weightless_orm.annotations.Sorts;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SorterBuilder {

  private List<String> fieldNames;
  private Map<String, Sort.Direction> directionMap;

  public SorterBuilder method(java.lang.reflect.Method method) {
    List<Sort> sortAnnotations = new ArrayList<>();
    for (Annotation methodLevelAnnotation : method.getDeclaredAnnotations()) {
      if (methodLevelAnnotation.annotationType().equals(Sorts.class)) {
        Sorts sortsAnnotation = (Sorts) methodLevelAnnotation;
        sortAnnotations.addAll(Arrays.asList(sortsAnnotation.value()));
      } else if (methodLevelAnnotation.annotationType().equals(Sort.class)) {
        Sort sortAnnotation = (Sort) methodLevelAnnotation;
        sortAnnotations.add(sortAnnotation);
      }
    }
    fieldNames = new ArrayList<>();
    directionMap = new HashMap<>();
    for (Sort sort : sortAnnotations) {
      fieldNames.add(sort.onField());
      directionMap.put(sort.onField(), sort.direction());
    }
    return this;
  }

  public Sorter build() {
    return new Sorter(fieldNames, directionMap);
  }
}
