package com.github.jackhallam.weightless_orm.mongo;

import com.github.jackhallam.weightless_orm.Weightless;
import com.github.jackhallam.weightless_orm.WeightlessORMException;
import com.github.jackhallam.weightless_orm.annotations.Bookmark;
import com.github.jackhallam.weightless_orm.annotations.Create;
import com.github.jackhallam.weightless_orm.annotations.Delete;
import com.github.jackhallam.weightless_orm.annotations.Field;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Equals;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class TestBookmark extends TestBase {
  public TestBookmark(Supplier<Weightless> weightlessSupplier) {
    super(weightlessSupplier);
  }

  @Test
  public void testBookmarkEmptySuccess() throws Exception {
    TestObject testObject = getDal(Dal.class).getBookmark();
    assertNull(testObject);
  }

  @Test
  public void testBookmarkSuccess() throws Exception {
    for (int i = 0; i < 5; i++) {
      TestObject testObject = new TestObject();
      testObject.testField = i;
      getDal(Dal.class).create(testObject);
    }

    for (int i = 0; i < 5; i++) {
      TestObject testObject = getDal(Dal.class).getBookmark();
      assertEquals(i, testObject.testField);
    }

    for (int i = 0; i < 5; i++) {
      TestObject testObject = getDal(Dal.class).getBookmarkFromBookmarkId("someid");
      assertEquals(i, testObject.testField);
    }
  }

  @Test
  public void testBookmarkOptionalSuccess() throws Exception {
    assertFalse(getDal(Dal.class).getBookmarkAsOptional().isPresent());

    TestObject testObject = new TestObject();
    testObject.testField = 1;
    getDal(Dal.class).create(testObject);

    Optional<TestObject> found = getDal(Dal.class).getBookmarkAsOptional();
    assertEquals(1, found.get().testField);
  }

  @Test
  public void testBookmarkReturnListSuccess() throws Exception {
    assertTrue(getDal(Dal.class).getBookmarkAsList().isEmpty());

    TestObject testObject = new TestObject();
    testObject.testField = 1;
    getDal(Dal.class).create(testObject);

    List<TestObject> found = getDal(Dal.class).getBookmarkAsList();
    assertEquals(1, found.get(0).testField);
  }

  @Test
  public void testBookmarkReturnIterableSuccess() throws Exception {
    Iterator<TestObject> iterator = getDal(Dal.class).getBookmarkAsIterable().iterator();
    assertFalse(iterator.hasNext());

    TestObject testObject = new TestObject();
    testObject.testField = 1;
    getDal(Dal.class).create(testObject);

    iterator = getDal(Dal.class).getBookmarkAsIterable().iterator();
    assertTrue(iterator.hasNext());
    assertEquals(1, iterator.next().testField);
  }

  @Test
  public void testBookmarkOverflowSuccess() throws Exception {
    for (int i = 0; i < 5; i++) {
      TestObject testObject = new TestObject();
      testObject.testField = i;
      getDal(Dal.class).create(testObject);
    }

    for (int i = 0; i < 5; i++) {
      getDal(Dal.class).getBookmark();
    }

    TestObject testObject = getDal(Dal.class).getBookmark();
    assertEquals(0, testObject.testField);
  }

  @Test
  public void testBookmarkElementRemovedOverflowSuccess() throws Exception {
    for (int i = 0; i < 5; i++) {
      TestObject testObject = new TestObject();
      testObject.testField = i;
      getDal(Dal.class).create(testObject);
    }

    getDal(Dal.class).getBookmark();
    getDal(Dal.class).getBookmark();
    getDal(Dal.class).getBookmark();


    for (int i = 0; i < 4; i++) {
        getDal(Dal.class).deleteByTestField(i);
    }

    TestObject testObject = getDal(Dal.class).getBookmark();
    assertEquals(4, testObject.testField);
  }

  @Test
  public void testBookmarkTooManyArgumentsFailure() throws Exception {
    assertThrows(WeightlessORMException.class, () -> getDal(Dal.class).failBookmarkTooManyArguments("foo", "bar"));
  }

  @Test
  public void testBookmarkArgumentNotStringFailure() throws Exception {
    assertThrows(WeightlessORMException.class, () -> getDal(Dal.class).failBookmarkArgumentNotString(1));
  }

  @Test
  public void testBookmarkVoidReturnFailure() throws Exception {
    assertThrows(WeightlessORMException.class, () -> getDal(Dal.class).failBookmarkVoidReturn());
  }

  @Test
  public void testBookmarkBooleanReturnFailure() throws Exception {
    assertThrows(WeightlessORMException.class, () -> getDal(Dal.class).failBookmarkBooleanReturn());
  }

  @Test
  public void testBookmarkReturnListOfListFailure() throws Exception {
    assertThrows(WeightlessORMException.class, () -> getDal(Dal.class).failBookmarkListOfListReturn());
  }

  public static class TestObject {
    public int testField;
  }

  public interface Dal {
    @Create
    TestObject create(TestObject testObject);

    @Delete
    TestObject deleteByTestField(@Field("testField") @Equals int testField);

    @Bookmark
    TestObject failBookmarkTooManyArguments(String arg1, String arg2);

    @Bookmark
    TestObject failBookmarkArgumentNotString(int arg1);

    @Bookmark
    void failBookmarkVoidReturn();

    @Bookmark
    boolean failBookmarkBooleanReturn();

    @Bookmark
    List<List<TestObject>> failBookmarkListOfListReturn();

    @Bookmark
    TestObject getBookmarkFromBookmarkId(String bookmarkId);

    @Bookmark
    TestObject getBookmark();

    @Bookmark
    Optional<TestObject> getBookmarkAsOptional();

    @Bookmark
    List<TestObject> getBookmarkAsList();

    @Bookmark
    Iterable<TestObject> getBookmarkAsIterable();
  }
}
