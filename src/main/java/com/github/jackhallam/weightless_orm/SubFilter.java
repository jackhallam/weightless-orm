package com.github.jackhallam.weightless_orm;

import java.lang.annotation.Annotation;

/**
 * SubFilter holds information related to a single filter parameter
 */
public class SubFilter<AnnotationType extends Annotation> {
  private String fieldName;
  private Object value;
  private AnnotationType filterTypeAnnotation;

  public SubFilter(String fieldName, Object value, AnnotationType filterTypeAnnotation) {
    this.fieldName = fieldName;
    this.value = value;
    this.filterTypeAnnotation = filterTypeAnnotation;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public AnnotationType getFilterTypeAnnotation() {
    return filterTypeAnnotation;
  }

  public void setFilterTypeAnnotation(AnnotationType filterTypeAnnotation) {
    this.filterTypeAnnotation = filterTypeAnnotation;
  }
}
