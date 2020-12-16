package com.github.jackhallam.weightless_orm;

import com.github.jackhallam.weightless_orm.persistents.MongoQuery;
import com.github.jackhallam.weightless_orm.persistents.PersistentStoreQuery;

import java.lang.annotation.Annotation;
import java.util.List;

public class Filterer {

  private List<SubFilter<? extends Annotation>> subFilters;

  public Filterer(List<SubFilter<? extends Annotation>> subFilters) {
    this.subFilters = subFilters;
  }

  public <T> void filter(PersistentStoreQuery<T> query) {
    for (SubFilter<? extends Annotation> subFilter : subFilters) {
      query.filter(subFilter.getFieldName(), subFilter.getFilterTypeAnnotation(), subFilter.getValue());
    }
  }
}
