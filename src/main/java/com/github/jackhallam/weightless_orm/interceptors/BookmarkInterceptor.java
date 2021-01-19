package com.github.jackhallam.weightless_orm.interceptors;

import com.github.jackhallam.weightless_orm.Weightless;
import com.github.jackhallam.weightless_orm.WeightlessORMException;
import com.github.jackhallam.weightless_orm.annotations.Field;
import com.github.jackhallam.weightless_orm.annotations.FindOrCreate;
import com.github.jackhallam.weightless_orm.annotations.Update;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Equals;
import com.github.jackhallam.weightless_orm.interceptors.handlers.ConditionHandler;
import com.github.jackhallam.weightless_orm.interceptors.handlers.ReturnHandler;
import com.github.jackhallam.weightless_orm.interceptors.handlers.SortHandler;
import com.github.jackhallam.weightless_orm.persistents.PersistentStore;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class BookmarkInterceptor {

  private static final String DEFAULT_BOOKMARK_ID = "DEFAULT";
  private final PersistentStore persistentStore;
  private final Weightless weightless;


  public BookmarkInterceptor(Weightless weightless, PersistentStore persistentStore) {
    this.weightless = weightless;
    this.persistentStore = persistentStore;
  }

  /**
   * Intercept a @Bookmark method
   */
  @RuntimeType
  public <T> Object intercept(@AllArguments Object[] allArguments, @Origin java.lang.reflect.Method method) {
    BookmarkReturnHandler<T> bookmarkReturnHandler = new BookmarkReturnHandler<>();

    // Infer the return type
    Class<T> clazz;
    try {
      clazz = (Class<T>) Class.forName(bookmarkReturnHandler.inferInnerTypeIfPresent((method.getGenericReturnType())).getTypeName());
    } catch (ClassNotFoundException e) {
      throw new WeightlessORMException(e);
    }

    String bookmarkId = getBookmarkId(method.getParameters(), allArguments);
    InternalBookmark internalBookmark = weightless.get(InternalBookmark.InternalBookmarkAccess.class).findOrCreateInternalBookmarkById(bookmarkId);

    Iterable<T> tIterable = persistentStore.find(clazz, new ConditionHandler(), new SortHandler<>());
    Iterator<T> tIterator = tIterable.iterator();

    // step through the iterator until the "next" is the one we care about
    boolean overflowed = false;
    for (int i = 0; i < internalBookmark.pointer; i++) {
      // We probably ran off the end
      if (!tIterator.hasNext()) {
        overflowed = true;
        break;
      }

      // Step through all but the one we care about
      tIterator.next();
    }

    if (overflowed || !tIterator.hasNext()) {
      // reset pointer back to the beginning
      internalBookmark.pointer = 0;

      // reset the iterator back to the beginning
      tIterable = persistentStore.find(clazz, new ConditionHandler(), new SortHandler<>());
      tIterator = tIterable.iterator();
    }

    List<T> output;
    if (!tIterator.hasNext()) {
      // No elements in the collection
      output = Collections.emptyList();
    } else {
      output = Collections.singletonList(tIterator.next());
    }

    internalBookmark.pointer++;

    weightless.get(InternalBookmark.InternalBookmarkAccess.class).updateInternalBookmarkById(internalBookmark, internalBookmark.id);

    return bookmarkReturnHandler.pick((Class<Object>) method.getReturnType()).apply(output);
  }

  private String getBookmarkId(Parameter[] parameters, Object[] allArguments) {
    String bookmarkId = DEFAULT_BOOKMARK_ID;
    if (parameters.length > 1) {
      throw new WeightlessORMException("Bookmark expects 0 or 1 parameter");
    }
    if (parameters.length == 1) {
      Parameter parameter = parameters[0];
      if (!parameter.getType().equals(String.class)) {
        throw new WeightlessORMException("Bookmark with one parameter expects a String bookmark id");
      }
      bookmarkId = (String) allArguments[0];
    }
    return bookmarkId;
  }

  public static class InternalBookmark {
    public String id;
    public int pointer;

    public interface InternalBookmarkAccess {
      @FindOrCreate
      InternalBookmark findOrCreateInternalBookmarkById(@Field("id") @Equals String id);

      @Update
      InternalBookmark updateInternalBookmarkById(InternalBookmark internalBookmark, @Field("id") @Equals String id);
    }
  }

  public class BookmarkReturnHandler<T> extends ReturnHandler<T> {

    @Override
    public void handleVoid(Iterable<T> tIterable) {
      throw new WeightlessORMException("Void not acceptable return type for bookmark");
    }

    @Override
    public boolean handleBoolean(Iterable<T> tIterable) {
      throw new WeightlessORMException("Void not acceptable return type for bookmark");
    }

    @Override
    public Iterable<T> handleIterable(Iterable<T> tIterable) {
      return tIterable;
    }

    @Override
    public Optional<T> handleOptional(Iterable<T> tIterable) {
      Iterator<T> iterator = tIterable.iterator();
      if (!iterator.hasNext()) {
        return Optional.empty();
      }
      return Optional.ofNullable(iterator.next());
    }

    @Override
    public T handlePojo(Iterable<T> tIterable) {
      Iterator<T> iterator = tIterable.iterator();
      if (!iterator.hasNext()) {
        return null;
      }
      return iterator.next();
    }
  }
}
