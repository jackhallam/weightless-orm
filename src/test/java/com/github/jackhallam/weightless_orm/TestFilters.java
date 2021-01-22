package com.github.jackhallam.weightless_orm;

import com.github.jackhallam.weightless_orm.annotations.Create;
import com.github.jackhallam.weightless_orm.annotations.Field;
import com.github.jackhallam.weightless_orm.annotations.Find;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Contains;
import com.github.jackhallam.weightless_orm.annotations.field_filters.ContainsIgnoreCase;
import com.github.jackhallam.weightless_orm.annotations.field_filters.DoesNotExist;
import com.github.jackhallam.weightless_orm.annotations.field_filters.EndsWith;
import com.github.jackhallam.weightless_orm.annotations.field_filters.EndsWithIgnoreCase;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Equals;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Exists;
import com.github.jackhallam.weightless_orm.annotations.field_filters.GreaterThan;
import com.github.jackhallam.weightless_orm.annotations.field_filters.GreaterThanOrEqualTo;
import com.github.jackhallam.weightless_orm.annotations.field_filters.HasAnyOf;
import com.github.jackhallam.weightless_orm.annotations.field_filters.HasNoneOf;
import com.github.jackhallam.weightless_orm.annotations.field_filters.LessThan;
import com.github.jackhallam.weightless_orm.annotations.field_filters.LessThanOrEqualTo;
import com.github.jackhallam.weightless_orm.annotations.field_filters.StartsWith;
import com.github.jackhallam.weightless_orm.annotations.field_filters.StartsWithIgnoreCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestFilters extends TestBase {
  public TestFilters(Supplier<Weightless> weightlessSupplier) {
    super(weightlessSupplier);
  }

  @Test
  public void testContainsSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.fieldOne = "defghi";
    testObject.fieldTwo = 5;
    getDal(Dal.class).create(testObject);

    TestObject found = getDal(Dal.class).findWhereFieldOneContains("defghi");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    found = getDal(Dal.class).findWhereFieldOneContains("def");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    found = getDal(Dal.class).findWhereFieldOneContains("ghi");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    found = getDal(Dal.class).findWhereFieldOneContains("fgh");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    TestObject notFound = getDal(Dal.class).findWhereFieldOneContains("Fgh");
    assertNull(notFound);

    notFound = getDal(Dal.class).findWhereFieldOneContains("abcd");
    assertNull(notFound);
  }

  @Test
  public void testContainsIgnoreCaseSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.fieldOne = "defghi";
    testObject.fieldTwo = 5;
    getDal(Dal.class).create(testObject);

    TestObject found = getDal(Dal.class).findWhereFieldOneContainsIgnoreCase("defghi");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    found = getDal(Dal.class).findWhereFieldOneContainsIgnoreCase("DeFgHi");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    found = getDal(Dal.class).findWhereFieldOneContainsIgnoreCase("Def");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    found = getDal(Dal.class).findWhereFieldOneContainsIgnoreCase("ghI");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    found = getDal(Dal.class).findWhereFieldOneContainsIgnoreCase("fgh");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    TestObject notFound = getDal(Dal.class).findWhereFieldOneContainsIgnoreCase("abcd");
    assertNull(notFound);

    notFound = getDal(Dal.class).findWhereFieldOneContainsIgnoreCase("abcD");
    assertNull(notFound);
  }

  @Test
  public void testDoesNotExistSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.fieldTwo = 5;
    getDal(Dal.class).create(testObject);

    TestObject found = getDal(Dal.class).findWhereFieldOneDoesNotExist(null);
    assertEquals(testObject.fieldTwo, found.fieldTwo);
  }

  @Test
  public void testEndsWithSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.fieldOne = "defghi";
    testObject.fieldTwo = 5;
    getDal(Dal.class).create(testObject);

    TestObject found = getDal(Dal.class).findWhereFieldOneEndsWith("defghi");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    found = getDal(Dal.class).findWhereFieldOneEndsWith("ghi");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    found = getDal(Dal.class).findWhereFieldOneEndsWith("i");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    TestObject notFound = getDal(Dal.class).findWhereFieldOneEndsWith("def");
    assertNull(notFound);

    notFound = getDal(Dal.class).findWhereFieldOneEndsWith("Ghi");
    assertNull(notFound);
  }

  @Test
  public void testEndsWithIgnoreCaseSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.fieldOne = "defghi";
    testObject.fieldTwo = 5;
    getDal(Dal.class).create(testObject);

    TestObject found = getDal(Dal.class).findWhereFieldOneEndsWithIgnoreCase("defghi");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    found = getDal(Dal.class).findWhereFieldOneEndsWithIgnoreCase("gHi");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    found = getDal(Dal.class).findWhereFieldOneEndsWithIgnoreCase("I");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    TestObject notFound = getDal(Dal.class).findWhereFieldOneEndsWithIgnoreCase("def");
    assertNull(notFound);

    notFound = getDal(Dal.class).findWhereFieldOneEndsWithIgnoreCase("fgh");
    assertNull(notFound);
  }

  @Test
  public void testEqualsSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.fieldOne = "abc";
    testObject.fieldTwo = 5;
    getDal(Dal.class).create(testObject);

    TestObject found = getDal(Dal.class).findWhereFieldOneEquals("abc");
    assertEquals(testObject.fieldOne, found.fieldOne);
    assertEquals(testObject.fieldTwo, found.fieldTwo);
  }

  @Test
  public void testExistsSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.fieldOne = "abc";
    testObject.fieldTwo = 5;
    getDal(Dal.class).create(testObject);

    TestObject found = getDal(Dal.class).findWhereFieldOneExists(null);
    assertEquals(testObject.fieldTwo, found.fieldTwo);
  }

  @Test
  public void testGreaterThanSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.fieldOne = "abc";
    testObject.fieldTwo = 5;
    getDal(Dal.class).create(testObject);

    TestObject found = getDal(Dal.class).findWhereFieldTwoGreaterThan(4);
    assertEquals(testObject.fieldOne, found.fieldOne);

    TestObject notFound = getDal(Dal.class).findWhereFieldTwoGreaterThan(5);
    assertNull(notFound);

    notFound = getDal(Dal.class).findWhereFieldTwoGreaterThan(6);
    assertNull(notFound);
  }

  @Test
  public void testGreaterThanOrEqualToSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.fieldOne = "abc";
    testObject.fieldTwo = 5;
    getDal(Dal.class).create(testObject);

    TestObject found = getDal(Dal.class).findWhereFieldTwoGreaterThanOrEqualTo(4);
    assertEquals(testObject.fieldOne, found.fieldOne);

    found = getDal(Dal.class).findWhereFieldTwoGreaterThanOrEqualTo(5);
    assertEquals(testObject.fieldOne, found.fieldOne);

    TestObject notFound = getDal(Dal.class).findWhereFieldTwoGreaterThanOrEqualTo(6);
    assertNull(notFound);
  }

  @Test
  public void testHasAnyOfSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.fieldOne = "abc";
    testObject.fieldTwo = 5;
    getDal(Dal.class).create(testObject);

    TestObject found = getDal(Dal.class).findWhereFieldOneHasAnyOf(Arrays.asList("zzz", "abc", "123"));
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    TestObject notFound = getDal(Dal.class).findWhereFieldOneHasAnyOf(Arrays.asList("zzz", "123"));
    assertNull(notFound);
  }

  @Test
  public void testHasNoneOfSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.fieldOne = "abc";
    testObject.fieldTwo = 5;
    getDal(Dal.class).create(testObject);

    TestObject found = getDal(Dal.class).findWhereFieldOneHasNoneOf(Arrays.asList("zzz", "123"));
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    TestObject notFound = getDal(Dal.class).findWhereFieldOneHasNoneOf(Arrays.asList("zzz", "abc", "123"));
    assertNull(notFound);
  }

  @Test
  public void testLessThanSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.fieldOne = "abc";
    testObject.fieldTwo = 5;
    getDal(Dal.class).create(testObject);

    TestObject found = getDal(Dal.class).findWhereFieldTwoLessThan(6);
    assertEquals(testObject.fieldOne, found.fieldOne);

    TestObject notFound = getDal(Dal.class).findWhereFieldTwoLessThan(5);
    assertNull(notFound);

    notFound = getDal(Dal.class).findWhereFieldTwoLessThan(4);
    assertNull(notFound);
  }

  @Test
  public void testLessThanOrEqualToSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.fieldOne = "abc";
    testObject.fieldTwo = 5;
    getDal(Dal.class).create(testObject);

    TestObject found = getDal(Dal.class).findWhereFieldTwoLessThanOrEqualTo(6);
    assertEquals(testObject.fieldOne, found.fieldOne);

    found = getDal(Dal.class).findWhereFieldTwoLessThanOrEqualTo(5);
    assertEquals(testObject.fieldOne, found.fieldOne);

    TestObject notFound = getDal(Dal.class).findWhereFieldTwoLessThanOrEqualTo(4);
    assertNull(notFound);
  }


  @Test
  public void testStartsWithSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.fieldOne = "defghi";
    testObject.fieldTwo = 5;
    getDal(Dal.class).create(testObject);

    TestObject found = getDal(Dal.class).findWhereFieldOneStartsWith("defghi");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    found = getDal(Dal.class).findWhereFieldOneStartsWith("def");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    found = getDal(Dal.class).findWhereFieldOneStartsWith("d");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    TestObject notFound = getDal(Dal.class).findWhereFieldOneStartsWith("ghi");
    assertNull(notFound);

    notFound = getDal(Dal.class).findWhereFieldOneStartsWith("Def");
    assertNull(notFound);
  }

  @Test
  public void testStartsWithIgnoreCaseSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.fieldOne = "defghi";
    testObject.fieldTwo = 5;
    getDal(Dal.class).create(testObject);

    TestObject found = getDal(Dal.class).findWhereFieldOneStartsWithIgnoreCase("defghi");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    found = getDal(Dal.class).findWhereFieldOneStartsWithIgnoreCase("dEf");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    found = getDal(Dal.class).findWhereFieldOneStartsWithIgnoreCase("D");
    assertEquals(testObject.fieldTwo, found.fieldTwo);

    TestObject notFound = getDal(Dal.class).findWhereFieldOneStartsWithIgnoreCase("ghi");
    assertNull(notFound);

    notFound = getDal(Dal.class).findWhereFieldOneStartsWithIgnoreCase("fgh");
    assertNull(notFound);
  }

  public static class TestObject {
    public String fieldOne;
    public int fieldTwo;
  }

  public interface Dal {
    @Find
    TestObject findWhereFieldOneContains(@Field("fieldOne") @Contains String part);

    @Find
    TestObject findWhereFieldOneContainsIgnoreCase(@Field("fieldOne") @ContainsIgnoreCase String part);

    @Find
    TestObject findWhereFieldOneDoesNotExist(@Field("fieldOne") @DoesNotExist Void na);

    @Find
    TestObject findWhereFieldOneEndsWith(@Field("fieldOne") @EndsWith String part);

    @Find
    TestObject findWhereFieldOneEndsWithIgnoreCase(@Field("fieldOne") @EndsWithIgnoreCase String part);

    @Find
    TestObject findWhereFieldOneEquals(@Field("fieldOne") @Equals String fieldOne);

    @Find
    TestObject findWhereFieldOneExists(@Field("fieldOne") @Exists Void na);

    @Find
    TestObject findWhereFieldTwoGreaterThan(@Field("fieldTwo") @GreaterThan int fieldTwo);

    @Find
    TestObject findWhereFieldTwoGreaterThanOrEqualTo(@Field("fieldTwo") @GreaterThanOrEqualTo int fieldTwo);

    @Find
    TestObject findWhereFieldOneHasAnyOf(@Field("fieldOne") @HasAnyOf List<String> options);

    @Find
    TestObject findWhereFieldOneHasNoneOf(@Field("fieldOne") @HasNoneOf List<String> options);

    @Find
    TestObject findWhereFieldTwoLessThan(@Field("fieldTwo") @LessThan int fieldTwo);

    @Find
    TestObject findWhereFieldTwoLessThanOrEqualTo(@Field("fieldTwo") @LessThanOrEqualTo int fieldTwo);

    @Find
    TestObject findWhereFieldOneStartsWith(@Field("fieldOne") @StartsWith String part);

    @Find
    TestObject findWhereFieldOneStartsWithIgnoreCase(@Field("fieldOne") @StartsWithIgnoreCase String part);

    @Create
    TestObject create(TestObject testObject);
  }
}
