package com.github.jackhallam.weightless_orm.mongo;

import com.github.jackhallam.weightless_orm.Weightless;
import com.github.jackhallam.weightless_orm.WeightlessORMException;
import com.github.jackhallam.weightless_orm.annotations.Create;
import com.github.jackhallam.weightless_orm.annotations.Field;
import com.github.jackhallam.weightless_orm.annotations.Find;
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

public class TestFind extends TestBase {
  public TestFind(Supplier<Weightless> weightlessSupplier) {
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
  public void testFindAllSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    testObject = getDal(Dal.class).create(testObject);
    List<TestObject> foundTestObjects = getDal(Dal.class).findAll();

    assertEquals(1, foundTestObjects.size());
    assertEquals(testObject.testField, foundTestObjects.get(0).testField);
  }

  @Test
  public void testFindReturnListOfListsFailure() throws Exception {
    assertThrows(WeightlessORMException.class, () -> getDal(Dal.class).failureFindReturnListOfLists("somestring"));
  }

  @Test
  public void testFindReturnVoidFailure() throws Exception {
    assertThrows(WeightlessORMException.class, () -> getDal(Dal.class).failureFindReturnVoid("somestring"));
  }

  @Test
  public void testFindReturnBooleanFailure() throws Exception {
    assertThrows(WeightlessORMException.class, () -> getDal(Dal.class).failureFindReturnBoolean("somestring"));
  }

  @Test
  public void testFindReturnIterableSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    testObject = getDal(Dal.class).create(testObject);
    Iterable<TestObject> iterable = getDal(Dal.class).findReturnIterable("hello");
    Iterator<TestObject> iterator = iterable.iterator();

    assertTrue(iterator.hasNext());
    assertEquals(testObject.testField, iterator.next().testField);
  }

  @Test
  public void testFindReturnOptionalSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    testObject = getDal(Dal.class).create(testObject);
    Optional<TestObject> testObjectOptional = getDal(Dal.class).findReturnOptional("hello");

    assertTrue(testObjectOptional.isPresent());
    assertEquals(testObject.testField, testObjectOptional.get().testField);
  }

  @Test
  public void testFindReturnOptionalEmptySuccess() throws Exception {
    Optional<TestObject> testObjectOptional = getDal(Dal.class).findReturnOptional("hello");

    assertFalse(testObjectOptional.isPresent());
  }

  public static class TestObject {
    public String testField;
  }

  public interface Dal {
    @Find
    TestObject find(@Field("testField") @Equals String testField);

    @Find
    Iterable<TestObject> findReturnIterable(@Field("testField") @Equals String testField);

    @Find
    Optional<TestObject> findReturnOptional(@Field("testField") @Equals String testField);

    @Find
    List<List<TestObject>> failureFindReturnListOfLists(@Field("testField") @Equals String testField);

    @Find
    void failureFindReturnVoid(@Field("testField") @Equals String testField);

    @Find
    boolean failureFindReturnBoolean(@Field("testField") @Equals String testField);

    @Find
    List<TestObject> findAll();

    @Create
    TestObject create(TestObject testObject);
  }
}
