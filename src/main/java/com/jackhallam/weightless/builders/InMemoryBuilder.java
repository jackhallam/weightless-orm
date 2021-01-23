package com.jackhallam.weightless.builders;

import com.jackhallam.weightless.Weightless;
import com.jackhallam.weightless.persistents.InMemoryPersistentStore;
import com.jackhallam.weightless.persistents.PersistentStore;

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
