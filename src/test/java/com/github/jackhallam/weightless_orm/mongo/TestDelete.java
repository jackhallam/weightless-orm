package com.github.jackhallam.weightless_orm.mongo;

import com.github.jackhallam.weightless_orm.Weightless;
import com.github.jackhallam.weightless_orm.annotations.Create;
import com.github.jackhallam.weightless_orm.annotations.Delete;
import com.github.jackhallam.weightless_orm.annotations.Field;
import com.github.jackhallam.weightless_orm.annotations.Find;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Equals;
import org.junit.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestDelete extends TestBase {
  public TestDelete(Supplier<Weightless> weightlessSupplier) {
    super(weightlessSupplier);
  }

  @Test
  public void testDeleteSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    testObject.otherTestField = 1;
    testObject = getDal(Dal.class).create(testObject);

    TestObject deleted = getDal(Dal.class).delete("hello");
    assertNotNull(deleted);

    List<TestObject> found = getDal(Dal.class).findAll();
    assertTrue(found.isEmpty());
  }

  @Test
  public void testDeleteNotFoundSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    testObject.otherTestField = 1;
    testObject = getDal(Dal.class).create(testObject);

    TestObject notFoundTestObject = new TestObject();
    notFoundTestObject.testField = "world";
    notFoundTestObject.otherTestField = 2;

    TestObject deleted = getDal(Dal.class).delete("world");
    assertNull(deleted);

    List<TestObject> found = getDal(Dal.class).findAll();
    assertEquals(testObject.testField, found.get(0).testField);
    assertEquals(testObject.otherTestField, found.get(0).otherTestField);
  }

  @Test
  public void testDeleteEmptyDatabaseSuccess() throws Exception {
    TestObject deleted = getDal(Dal.class).delete("world");
    assertNull(deleted);
  }

  public static class TestObject {
    public String testField;
    public int otherTestField;
  }

  public interface Dal {
    @Find
    List<TestObject> findAll();

    @Delete
    TestObject delete(@Field("testField") @Equals String testField);

    @Create
    TestObject create(TestObject testObject);
  }
}
