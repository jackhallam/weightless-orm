package com.github.jackhallam.weightless_orm.mongo;

import com.github.jackhallam.weightless_orm.Weightless;
import com.github.jackhallam.weightless_orm.annotations.Create;
import com.github.jackhallam.weightless_orm.annotations.Field;
import com.github.jackhallam.weightless_orm.annotations.FindOrCreate;
import com.github.jackhallam.weightless_orm.annotations.Sort;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Equals;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

    assertNotNull(foundTestObject.id);
    assertEquals(testObject.id, foundTestObject.id);
  }

  @Test
  public void testFindOrCreateOptionalSuccess() throws Exception {
    Optional<TestObject> testObjectOptional = getDal(Dal.class).findOrCreateOptional("the field value");

    assertTrue(testObjectOptional.isPresent());
    assertEquals("the field value", testObjectOptional.get().testField);
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
    assertEquals(secondTestObject.id, objects.get(0).id);
    assertEquals(firstTestObject.id, objects.get(1).id);
  }

  public static class TestObject {
    @Id
    public ObjectId id;
    public String testField;
    public int otherTestField;
  }

  public interface Dal {
    @FindOrCreate
    TestObject findOrCreate(@Field("testField") @Equals String testField);

    @FindOrCreate
    Optional<TestObject> findOrCreateOptional(@Field("testField") @Equals String testField);

    @FindOrCreate
    List<TestObject> findOrCreateReturnAll(@Field("testField") @Equals String testField);

    @FindOrCreate
    @Sort(onField = "otherTestField")
    List<TestObject> findOrCreateSorted(@Field("testField") @Equals String testField);

    @Create
    TestObject create(TestObject testObject);
  }
}
