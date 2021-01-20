package com.github.jackhallam.weightless_orm.mongo;

import com.github.jackhallam.weightless_orm.Weightless;
import com.github.jackhallam.weightless_orm.WeightlessORMException;
import com.github.jackhallam.weightless_orm.annotations.Create;
import com.github.jackhallam.weightless_orm.annotations.Field;
import com.github.jackhallam.weightless_orm.annotations.Find;
import com.github.jackhallam.weightless_orm.annotations.Update;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Equals;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

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
  public void testUpdateNoObjectsFailure() throws Exception {
    assertThrows(WeightlessORMException.class, () -> getDal(Dal.class).failureUpdateList(Collections.emptyList(), "hello"));
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

    assertThrows(WeightlessORMException.class, () -> getDal(Dal.class).failureUpdateList(testObjects, "hello"));
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
    TestObject failureUpdateList(List<TestObject> testObject, @Field("testField") @Equals String testField);

    @Create
    TestObject create(TestObject testObject);
  }
}
