package com.github.jackhallam.weightless_orm;

import com.github.jackhallam.weightless_orm.annotations.Field;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class FiltererBuilder {

  private List<SubFilter<?>> subFilters;

  public FiltererBuilder parameters(List<Parameter<?>> parameters) {
    subFilters = new ArrayList<>();
    for (Parameter<?> parameter : parameters) {
      List<? extends Annotation> annotations = parameter.getAnnotations();
      if (annotations.isEmpty()) {
        continue; // Skip if there are not any annotations (such as the @Update case)
      }
      if (annotations.size() != 2) {
        throw new WeightlessORMException("Expected 2 annotations on the parameter but found " + annotations.size());
      }
      if (!annotations.get(0).annotationType().equals(Field.class)) {
        throw new WeightlessORMException("Expected " + Field.class + " as the first annotation on the parameter but found " + annotations.get(0).annotationType());
      }

      String fieldName = ((Field) annotations.get(0)).value();
      Object value = parameter.getValue();

      SubFilter<? extends Annotation> subFilter = new SubFilter<>(fieldName, value, annotations.get(1));
      subFilters.add(subFilter);
    }
    return this;
  }

  public Filterer build() {
    return new Filterer(subFilters);
  }
}
