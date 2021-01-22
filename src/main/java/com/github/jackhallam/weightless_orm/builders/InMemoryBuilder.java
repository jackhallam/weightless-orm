package com.github.jackhallam.weightless_orm.builders;

import com.github.jackhallam.weightless_orm.Weightless;
import com.github.jackhallam.weightless_orm.persistents.InMemoryPersistentStore;
import com.github.jackhallam.weightless_orm.persistents.PersistentStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryBuilder {
  public Weightless build() {
    Map<Class<?>, List<?>> mapping = new HashMap<>();
    PersistentStore persistenceStore = new InMemoryPersistentStore(mapping);
    return new Weightless(persistenceStore);
  }
}
