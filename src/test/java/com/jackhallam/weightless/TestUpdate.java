package com.jackhallam.weightless;

import com.jackhallam.weightless.annotations.Create;
import com.jackhallam.weightless.annotations.Field;
import com.jackhallam.weightless.annotations.Find;
import com.jackhallam.weightless.annotations.Update;
import com.jackhallam.weightless.annotations.field_filters.Equals;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class TestUpdate extends TestBase {

  public TestUpdate(Supplier<Weightless> weightlessSupplier) {
    super(weightlessSupplier);
  }

  @Test
  public void testUpdateSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    testObject.secondTestField = 2;
    testObject = getDal(Dal.class).create(testObject);

    testObject.secondTestField = 3;

    getDal(Dal.class).update(testObject, "hello");

    List<TestObject> found = getDal(Dal.class).findAll();
    assertEquals(1, found.size());
    assertEquals("hello", found.get(0).testField);
    assertEquals(3, found.get(0).secondTestField);
  }

  @Test
  public void testUpdateEmptySuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    testObject.secondTestField = 2;
    testObject = getDal(Dal.class).create(testObject);

    testObject.secondTestField = 3;

    assertNull(getDal(Dal.class).update(testObject, "world"));
  }

  @Test
  public void testUpdateReturnVoidSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    testObject.secondTestField = 2;
    testObject = getDal(Dal.class).create(testObject);

    testObject.secondTestField = 3;

    getDal(Dal.class).updateReturnVoid(testObject, "hello");

    List<TestObject> found = getDal(Dal.class).findAll();
    assertEquals(1, found.size());
    assertEquals("hello", found.get(0).testField);
    assertEquals(3, found.get(0).secondTestField);
  }

  @Test
  public void testUpdateReturnVoidEmptyFailure() throws Exception {
    assertThrows(WeightlessException.class, () -> getDal(Dal.class).updateReturnVoid(new TestObject(), "world"));
  }

  @Test
  public void testUpdateReturnBooleanSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    testObject.secondTestField = 2;
    testObject = getDal(Dal.class).create(testObject);

    testObject.secondTestField = 3;

    assertTrue(getDal(Dal.class).updateReturnBoolean(testObject, "hello"));

    List<TestObject> found = getDal(Dal.class).findAll();
    assertEquals(1, found.size());
    assertEquals("hello", found.get(0).testField);
    assertEquals(3, found.get(0).secondTestField);
  }

  @Test
  public void testUpdateReturnIterableSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    testObject.secondTestField = 2;
    testObject = getDal(Dal.class).create(testObject);

    testObject.secondTestField = 3;

    assertTrue(getDal(Dal.class).updateReturnIterable(testObject, "hello").iterator().hasNext());

    List<TestObject> found = getDal(Dal.class).findAll();
    assertEquals(1, found.size());
    assertEquals("hello", found.get(0).testField);
    assertEquals(3, found.get(0).secondTestField);
  }

  @Test
  public void testUpdateReturnOptionalSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    testObject.secondTestField = 2;
    testObject = getDal(Dal.class).create(testObject);

    testObject.secondTestField = 3;

    assertTrue(getDal(Dal.class).updateReturnOptional(testObject, "hello").isPresent());

    List<TestObject> found = getDal(Dal.class).findAll();
    assertEquals(1, found.size());
    assertEquals("hello", found.get(0).testField);
    assertEquals(3, found.get(0).secondTestField);
  }

  @Test
  public void testUpdateReturnOptionalEmptySuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    testObject.secondTestField = 2;
    testObject = getDal(Dal.class).create(testObject);

    testObject.secondTestField = 3;

    assertFalse(getDal(Dal.class).updateReturnOptional(testObject, "world").isPresent());
  }

  @Test
  public void testUpdateNoObjectsFailure() throws Exception {
    assertThrows(WeightlessException.class, () -> getDal(Dal.class).failureUpdateList(Collections.emptyList(), "hello"));
  }

  @Test
  public void testUpdateTooManyObjectsFailure() throws Exception {
    TestObject firstTestObject = new TestObject();
    firstTestObject.testField = "hello";
    firstTestObject.secondTestField = 1;
    firstTestObject = getDal(Dal.class).create(firstTestObject);

    firstTestObject.secondTestField = 3;

    TestObject secondTestObject = new TestObject();
    secondTestObject.testField = "hello";
    secondTestObject.secondTestField = 2;
    secondTestObject = getDal(Dal.class).create(secondTestObject);

    secondTestObject.secondTestField = 4;

    List<TestObject> testObjects = Arrays.asList(firstTestObject, secondTestObject);

    assertThrows(WeightlessException.class, () -> getDal(Dal.class).failureUpdateList(testObjects, "hello"));
  }

  public static class TestObject {
    public String testField;
    public int secondTestField;
  }

  public interface Dal {

    @Find
    List<TestObject> findAll();

    @Update
    TestObject update(TestObject testObject, @Field("testField") @Equals String testField);

    @Update
    void updateReturnVoid(TestObject testObject, @Field("testField") @Equals String testField);

    @Update
    boolean updateReturnBoolean(TestObject testObject, @Field("testField") @Equals String testField);

    @Update
    Iterable<TestObject> updateReturnIterable(TestObject testObject, @Field("testField") @Equals String testField);

    @Update
    Optional<TestObject> updateReturnOptional(TestObject testObject, @Field("testField") @Equals String testField);

    @Update
    TestObject failureUpdateList(List<TestObject> testObject, @Field("testField") @Equals String testField);

    @Create
    TestObject create(TestObject testObject);
  }
}
