package com.github.jackhallam.weightless_orm.mongo;


import com.github.jackhallam.weightless_orm.Weightless;
import com.github.jackhallam.weightless_orm.WeightlessORMException;
import com.github.jackhallam.weightless_orm.annotations.Create;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
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
  public void testCreateAllVoidSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    getDal(Dal.class).createAllReturnVoid(Collections.singletonList(testObject));
  }

  @Test
  public void testCreateNoneErrorVoidFailure() throws Exception {
    assertThrows(WeightlessORMException.class, () -> getDal(Dal.class).createAllReturnVoid(Collections.emptyList()));
  }

  @Test
  public void testCreateBooleanSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    assertTrue(getDal(Dal.class).createReturnBoolean(testObject));
  }

  public static class TestObject {
    public String testField;
  }

  public interface Dal {
    @Create
    TestObject create(TestObject testObject);

    @Create
    List<TestObject> createAll(List<TestObject> testObjects);

    @Create
    void createAllReturnVoid(List<TestObject> testObjects);

    @Create
    boolean createReturnBoolean(TestObject testObject);
  }
}
