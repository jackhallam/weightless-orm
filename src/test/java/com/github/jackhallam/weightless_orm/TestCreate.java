package com.github.jackhallam.weightless_orm;


import com.github.jackhallam.weightless_orm.annotations.Create;
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

public class TestCreate extends TestBase {

  public TestCreate(Supplier<Weightless> weightlessSupplier) {
    super(weightlessSupplier);
  }

  @Test
  public void testCreateOneSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    TestObject savedTestObject = getDal(Dal.class).create(testObject);

    assertEquals(testObject.testField, savedTestObject.testField);
  }

  @Test
  public void testCreateMultipleSuccess() throws Exception {
    TestObject firstTestObject = new TestObject();
    firstTestObject.testField = "hello";
    TestObject secondTestObject = new TestObject();
    secondTestObject.testField = "world";

    List<TestObject> savedTestObjects = getDal(Dal.class).createAll(Arrays.asList(firstTestObject, secondTestObject));
    assertEquals(2, savedTestObjects.size());
    assertEquals(firstTestObject.testField, savedTestObjects.get(0).testField);
    assertEquals(secondTestObject.testField, savedTestObjects.get(1).testField);
  }

  @Test
  public void testCreateReturnVoidSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    getDal(Dal.class).createAllReturnVoid(Collections.singletonList(testObject));
  }

  @Test
  public void testCreateNoneSuccess() throws Exception {
    TestObject testObject = getDal(Dal.class).create(Collections.emptyList());
    assertNull(testObject);
  }

  @Test
  public void testCreateNoneReturnVoidFailure() throws Exception {
    assertThrows(WeightlessException.class, () -> getDal(Dal.class).createAllReturnVoid(Collections.emptyList()));
  }

  @Test
  public void testCreateReturnBooleanSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    assertTrue(getDal(Dal.class).createReturnBoolean(testObject));
  }

  @Test
  public void testCreateReturnObjectBooleanSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    assertTrue(getDal(Dal.class).createReturnObjectBoolean(testObject));
  }

  @Test
  public void testCreateReturnIterableSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    assertTrue(getDal(Dal.class).createReturnIterable(testObject).iterator().hasNext());
    assertEquals(testObject.testField, getDal(Dal.class).createReturnIterable(testObject).iterator().next().testField);
  }

  @Test
  public void testCreateReturnOptionalSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    assertTrue(getDal(Dal.class).createAllReturnOptional(Collections.singletonList(testObject)).isPresent());
  }

  @Test
  public void testCreateReturnOptionalEmptySuccess() throws Exception {
    assertFalse(getDal(Dal.class).createAllReturnOptional(Collections.emptyList()).isPresent());
  }

  @Test
  public void testCreateReturnListEmptySuccess() throws Exception {
    assertTrue(getDal(Dal.class).createAll(Collections.emptyList()).isEmpty());
  }

  public static class TestObject {
    public String testField;
  }

  public interface Dal {
    @Create
    TestObject create(TestObject testObject);

    @Create
    TestObject create(List<TestObject> testObject);

    @Create
    Iterable<TestObject> createReturnIterable(TestObject testObject);

    @Create
    List<TestObject> createAll(List<TestObject> testObjects);

    @Create
    void createAllReturnVoid(List<TestObject> testObjects);

    @Create
    boolean createReturnBoolean(TestObject testObject);

    @Create
    Boolean createReturnObjectBoolean(TestObject testObject);

    @Create
    Optional<TestObject> createAllReturnOptional(List<TestObject> testObjects);
  }
}
