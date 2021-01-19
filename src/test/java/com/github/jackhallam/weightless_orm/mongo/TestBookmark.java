package com.github.jackhallam.weightless_orm.mongo;

import com.github.jackhallam.weightless_orm.Weightless;
import com.github.jackhallam.weightless_orm.annotations.Bookmark;
import com.github.jackhallam.weightless_orm.annotations.Create;
import com.github.jackhallam.weightless_orm.annotations.Delete;
import com.github.jackhallam.weightless_orm.annotations.Field;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Equals;
import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class TestBookmark extends TestBase {
  public TestBookmark(Supplier<Weightless> weightlessSupplier) {
    super(weightlessSupplier);
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

    for (int i = 0; i < 5; i++) {
      if (i == 4) {
        getDal(Dal.class).deleteByTestField(i);
      } else {
        getDal(Dal.class).getBookmark();
      }
    }

    TestObject testObject = getDal(Dal.class).getBookmark();
    assertEquals(0, testObject.testField);
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
    TestObject getBookmarkFromBookmarkId(String bookmarkId);

    @Bookmark
    TestObject getBookmark();
  }
}
