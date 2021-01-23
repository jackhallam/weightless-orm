package com.jackhallam.weightless;

import com.jackhallam.weightless.annotations.Create;
import com.jackhallam.weightless.annotations.Field;
import com.jackhallam.weightless.annotations.Find;
import com.jackhallam.weightless.annotations.Sort;
import com.jackhallam.weightless.annotations.field_filters.Equals;
import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class TestSort extends TestBase {

  public TestSort(Supplier<Weightless> weightlessSupplier) {
    super(weightlessSupplier);
  }

  @Test
  public void testSingleSortSuccess() throws Exception {
    TestObject firstTestObject = new TestObject();
    firstTestObject.testField = "abc";
    firstTestObject.secondTestField = 3;
    getDal(Dal.class).create(firstTestObject);
    TestObject secondTestObject = new TestObject();
    secondTestObject.testField = "abc";
    secondTestObject.secondTestField = 2;
    secondTestObject = getDal(Dal.class).create(secondTestObject);
    TestObject found = getDal(Dal.class).findSortBySecondTestField("abc");

    assertEquals(secondTestObject.testField, found.testField);
    assertEquals(secondTestObject.secondTestField, found.secondTestField);
  }

  @Test
  public void testSingleSortDescendingSuccess() throws Exception {
    TestObject firstTestObject = new TestObject();
    firstTestObject.testField = "abc";
    firstTestObject.secondTestField = 2;
    getDal(Dal.class).create(firstTestObject);
    TestObject secondTestObject = new TestObject();
    secondTestObject.testField = "abc";
    secondTestObject.secondTestField = 3;
    secondTestObject = getDal(Dal.class).create(secondTestObject);
    TestObject found = getDal(Dal.class).findSortBySecondTestFieldDescending("abc");

    assertEquals(secondTestObject.testField, found.testField);
    assertEquals(secondTestObject.secondTestField, found.secondTestField);
  }

  @Test
  public void testMultipleSortSuccess() throws Exception {
    TestObject firstTestObject = new TestObject();
    firstTestObject.testField = "abc";
    firstTestObject.secondTestField = 1;
    firstTestObject.thirdTestField = 100;
    getDal(Dal.class).create(firstTestObject);
    TestObject secondTestObject = new TestObject();
    secondTestObject.testField = "abc";
    secondTestObject.secondTestField = 2;
    secondTestObject.thirdTestField = 0;
    TestObject thirdTestObject = new TestObject();
    thirdTestObject.testField = "abc";
    thirdTestObject.secondTestField = 1;
    thirdTestObject.thirdTestField = 99;
    thirdTestObject = getDal(Dal.class).create(thirdTestObject);
    TestObject found = getDal(Dal.class).findSortBySecondAndThirdTestFields("abc");

    assertEquals(thirdTestObject.testField, found.testField);
    assertEquals(thirdTestObject.secondTestField, found.secondTestField);
  }

  public static class TestObject {
    public String testField;
    public int secondTestField;
    public int thirdTestField;
  }

  public interface Dal {
    @Find
    @Sort(onField = "secondTestField")
    TestObject findSortBySecondTestField(@Field("testField") @Equals String testField);

    @Find
    @Sort(onField = "secondTestField", direction = Sort.Direction.DESCENDING)
    TestObject findSortBySecondTestFieldDescending(@Field("testField") @Equals String testField);

    @Find
    @Sort(onField = "secondTestField")
    @Sort(onField = "thirdTestField")
    TestObject findSortBySecondAndThirdTestFields(@Field("testField") @Equals String testField);

    @Create
    TestObject create(TestObject testObject);
  }
}
