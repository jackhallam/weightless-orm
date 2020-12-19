package com.github.jackhallam.weightless_orm;

import com.github.jackhallam.weightless_orm.annotations.Create;
import com.github.jackhallam.weightless_orm.annotations.Delete;
import com.github.jackhallam.weightless_orm.annotations.Find;
import com.github.jackhallam.weightless_orm.annotations.FindOrCreate;
import com.github.jackhallam.weightless_orm.annotations.Update;
import com.github.jackhallam.weightless_orm.interceptors.CreateOrUpdateInterceptor;
import com.github.jackhallam.weightless_orm.interceptors.DeleteInterceptor;
import com.github.jackhallam.weightless_orm.interceptors.FindInterceptor;
import com.github.jackhallam.weightless_orm.interceptors.FindOrCreateInterceptor;
import com.github.jackhallam.weightless_orm.persistents.PersistentStore;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Weightless implements Closeable {

  private final PersistentStore persistentStore;
  private final Map<String, Object> interceptedClassNameToObject;

  public Weightless(PersistentStore persistenceStore) {
    this.interceptedClassNameToObject = new HashMap<>();
    this.persistentStore = persistenceStore;
  }

  private <T> T getNewInstanceOf(Class<T> clazz) {
    try {
      Class<? extends T> dynamicType = new ByteBuddy()
        .subclass(clazz)
        .method(ElementMatchers.isAnnotatedWith(Create.class)
          .or(ElementMatchers.isAnnotatedWith(Update.class)))
        .intercept(MethodDelegation.to(new CreateOrUpdateInterceptor(persistentStore)))
        .method(ElementMatchers.isAnnotatedWith(Find.class))
        .intercept(MethodDelegation.to(new FindInterceptor(persistentStore)))
        .method(ElementMatchers.isAnnotatedWith(FindOrCreate.class))
        .intercept(MethodDelegation.to(new FindOrCreateInterceptor(persistentStore)))
        .method(ElementMatchers.isAnnotatedWith(Delete.class))
        .intercept(MethodDelegation.to(new DeleteInterceptor(persistentStore)))
        .make()
        .load(getClass().getClassLoader())
        .getLoaded();
      return dynamicType.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new WeightlessORMException(e);
    }
  }

  public <T> T get(Class<T> clazz) {
    if (!interceptedClassNameToObject.containsKey(clazz.getName())) {
      interceptedClassNameToObject.put(clazz.getName(), getNewInstanceOf(clazz));
    }
    return (T) interceptedClassNameToObject.get(clazz.getName());
  }

  /**
   * Closes this stream and releases any system resources associated
   * with it. If the stream is already closed then invoking this
   * method has no effect.
   *
   * <p> As noted in {@link AutoCloseable#close()}, cases where the
   * close may fail require careful attention. It is strongly advised
   * to relinquish the underlying resources and to internally
   * <em>mark</em> the {@code Closeable} as closed, prior to throwing
   * the {@code IOException}.
   *
   * @throws IOException if an I/O error occurs
   */
  @Override
  public void close() throws IOException {
    if (persistentStore != null) {
      persistentStore.close();
    }
  }
}
