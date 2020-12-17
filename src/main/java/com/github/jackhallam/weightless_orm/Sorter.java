package com.github.jackhallam.weightless_orm;

import com.github.jackhallam.weightless_orm.annotations.Sort;
import com.github.jackhallam.weightless_orm.persistents.PersistentStoreQuery;

import java.util.List;
import java.util.Map;

public class Sorter {

  private List<String> fieldNames;
  private Map<String, Sort.Direction> directionMap;

  public Sorter(List<String> fieldNames, Map<String, Sort.Direction> directionMap) {
    this.fieldNames = fieldNames;
    this.directionMap = directionMap;
  }

  public <T> void sort(PersistentStoreQuery<T> query) {
    query.sort(fieldNames, directionMap);
  }
}
