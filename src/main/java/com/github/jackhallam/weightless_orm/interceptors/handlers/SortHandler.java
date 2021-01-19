package com.github.jackhallam.weightless_orm.interceptors.handlers;

import com.github.jackhallam.weightless_orm.annotations.Sort;
import com.github.jackhallam.weightless_orm.annotations.Sorts;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SortHandler<T> {

  private List<String> fieldNames;
  private Map<String, Sort.Direction> directionMap;

  public SortHandler() {
    this.fieldNames = new ArrayList<>();
    this.directionMap = new HashMap<>();
  }

  public SortHandler(java.lang.reflect.Method method) {
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
  }

  public Iterator<SortContainer> getSortsIterator() {
    Iterator<String> fieldNamesIterator = fieldNames.iterator();
    return new Iterator<SortContainer>() {
      @Override
      public boolean hasNext() {
        return fieldNamesIterator.hasNext();
      }

      @Override
      public SortContainer next() {
        SortContainer sortContainer = new SortContainer();
        sortContainer.fieldName = fieldNamesIterator.next();
        sortContainer.direction = directionMap.get(sortContainer.fieldName);
        return sortContainer;
      }
    };
  }

  public List<String> getFieldNames() {
    return fieldNames;
  }

  public Map<String, Sort.Direction> getDirectionMap() {
    return directionMap;
  }

  public static class SortContainer {
    public String fieldName;
    public Sort.Direction direction;
  }
}
