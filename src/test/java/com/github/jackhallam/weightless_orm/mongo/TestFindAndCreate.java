package com.github.jackhallam.weightless_orm.mongo;


import com.github.jackhallam.weightless_orm.Weightless;
import com.github.jackhallam.weightless_orm.annotations.Create;
import com.github.jackhallam.weightless_orm.annotations.Field;
import com.github.jackhallam.weightless_orm.annotations.Find;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Equals;
import org.junit.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestFindAndCreate extends TestBase {

  public TestFindAndCreate(Supplier<Weightless> weightlessSupplier) {
    super(weightlessSupplier);
  }

  @Test
  public void testFindEmptySuccess() throws Exception {
    TestObject testObject = getDal(Dal.class).find("non_existent");

    assertNull(testObject);
  }

  @Test
  public void testFindAllEmptySuccess() throws Exception {
    List<TestObject> testObjects = getDal(Dal.class).findAll();

    assertTrue(testObjects.isEmpty());
  }

  @Test
  public void testCreateOneFindOneSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    TestObject savedTestObject = getDal(Dal.class).create(testObject);
    TestObject foundTestObject = getDal(Dal.class).find("hello");

    assertEquals(savedTestObject.testField, foundTestObject.testField);
  }

  @Test
  public void testCreateOneFindAllSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    testObject = getDal(Dal.class).create(testObject);
    List<TestObject> foundTestObjects = getDal(Dal.class).findAll();

    assertEquals(1, foundTestObjects.size());
    assertEquals(testObject.testField, foundTestObjects.get(0).testField);
  }

  public static class TestObject {
    public String testField;
  }

  public interface Dal {
    @Find
    TestObject find(@Field("testField") @Equals String testField);

    @Find
    List<TestObject> findAll();

    @Create
    TestObject create(TestObject testObject);
  }
}
