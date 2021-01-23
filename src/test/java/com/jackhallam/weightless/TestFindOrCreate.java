package com.jackhallam.weightless;

import com.jackhallam.weightless.annotations.Create;
import com.jackhallam.weightless.annotations.Field;
import com.jackhallam.weightless.annotations.FindOrCreate;
import com.jackhallam.weightless.annotations.Sort;
import com.jackhallam.weightless.annotations.field_filters.Equals;
import com.jackhallam.weightless.annotations.field_filters.GreaterThan;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class TestFindOrCreate extends TestBase {

  public TestFindOrCreate(Supplier<Weightless> weightlessSupplier) {
    super(weightlessSupplier);
  }

  @Test
  public void testFindOrCreateCreatedSuccess() throws Exception {
    TestObject testObject = getDal(Dal.class).findOrCreate("the field value");

    assertEquals("the field value", testObject.testField);
  }

  @Test
  public void testFindOrCreateFoundSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "abc";
    testObject = getDal(Dal.class).create(testObject);
    TestObject foundTestObject = getDal(Dal.class).findOrCreate("abc");

    assertEquals(testObject.testField, foundTestObject.testField);
  }

  @Test
  public void testFindOrCreateReturnOptionalSuccess() throws Exception {
    Optional<TestObject> testObjectOptional = getDal(Dal.class).findOrCreateReturnOptional("the field value");

    assertTrue(testObjectOptional.isPresent());
    assertEquals("the field value", testObjectOptional.get().testField);
  }

  @Test
  public void testFindOrCreateReturnIterableSuccess() throws Exception {
    Iterable<TestObject> iterable = getDal(Dal.class).findOrCreateReturnIterable("the field value");

    assertTrue(iterable.iterator().hasNext());
    assertEquals("the field value", iterable.iterator().next().testField);
  }

  @Test
  public void testFindOrCreateReturnAllSuccess() throws Exception {
    TestObject firstTestObject = new TestObject();
    firstTestObject.testField = "abc";
    getDal(Dal.class).create(firstTestObject);
    TestObject secondTestObject = new TestObject();
    secondTestObject.testField = "def";
    getDal(Dal.class).create(secondTestObject);
    List<TestObject> objects = getDal(Dal.class).findOrCreateReturnAll("abc");

    assertEquals(1, objects.size());
  }

  @Test
  public void testFindOrCreateSortedSuccess() throws Exception {
    TestObject firstTestObject = new TestObject();
    firstTestObject.testField = "abc";
    firstTestObject.otherTestField = 2;
    firstTestObject = getDal(Dal.class).create(firstTestObject);
    TestObject secondTestObject = new TestObject();
    secondTestObject.testField = "abc";
    secondTestObject.otherTestField = 1;
    secondTestObject = getDal(Dal.class).create(secondTestObject);

    // It finds in this case
    List<TestObject> objects = getDal(Dal.class).findOrCreateSorted("abc");

    assertEquals(2, objects.size());
    assertEquals(secondTestObject.otherTestField, objects.get(0).otherTestField);
    assertEquals(firstTestObject.otherTestField, objects.get(1).otherTestField);
  }

  @Test
  public void testFindOrCreateFilterNotEqualsFailure() throws Exception {
    assertThrows(WeightlessException.class, () -> getDal(Dal.class).failureFindOrCreateNotEqualsFilter(2));
  }

  @Test
  public void testFindOrCreateReturnListOfListsFailure() throws Exception {
    assertThrows(WeightlessException.class, () -> getDal(Dal.class).failureFindOrCreateReturnListOfLists("hello"));
  }

  @Test
  public void testFindOrCreateReturnVoidFailure() throws Exception {
    assertThrows(WeightlessException.class, () -> getDal(Dal.class).failureFindOrCreateReturnVoid("hello"));
  }

  @Test
  public void testFindOrCreateReturnBooleanFailure() throws Exception {
    assertThrows(WeightlessException.class, () -> getDal(Dal.class).failureFindOrCreateReturnBoolean("hello"));
  }

  public static class TestObject {
    public String testField;
    public int otherTestField;
  }

  public interface Dal {
    @FindOrCreate
    TestObject findOrCreate(@Field("testField") @Equals String testField);

    @FindOrCreate
    TestObject failureFindOrCreateNotEqualsFilter(@Field("otherTestField") @GreaterThan int val);

    @FindOrCreate
    List<List<TestObject>> failureFindOrCreateReturnListOfLists(@Field("testField") @Equals String testField);

    @FindOrCreate
    void failureFindOrCreateReturnVoid(@Field("testField") @Equals String testField);

    @FindOrCreate
    boolean failureFindOrCreateReturnBoolean(@Field("testField") @Equals String testField);

    @FindOrCreate
    Optional<TestObject> findOrCreateReturnOptional(@Field("testField") @Equals String testField);

    @FindOrCreate
    Iterable<TestObject> findOrCreateReturnIterable(@Field("testField") @Equals String testField);

    @FindOrCreate
    List<TestObject> findOrCreateReturnAll(@Field("testField") @Equals String testField);

    @FindOrCreate
    @Sort(onField = "otherTestField")
    List<TestObject> findOrCreateSorted(@Field("testField") @Equals String testField);

    @Create
    TestObject create(TestObject testObject);
  }
}
