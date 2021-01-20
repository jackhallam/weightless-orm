package com.github.jackhallam.weightless_orm.mongo;

import com.github.jackhallam.weightless_orm.Weightless;
import com.github.jackhallam.weightless_orm.annotations.Create;
import com.github.jackhallam.weightless_orm.annotations.Field;
import com.github.jackhallam.weightless_orm.annotations.Find;
import com.github.jackhallam.weightless_orm.annotations.field_filters.DoesNotExist;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Exists;
import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class TestFilters extends TestBase {
  public TestFilters(Supplier<Weightless> weightlessSupplier) {
    super(weightlessSupplier);
  }

  @Test
  public void testExistsSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.fieldOne = "abc";
    testObject.fieldTwo = 5;
    getDal(Dal.class).create(testObject);
    TestObject found = getDal(Dal.class).findObjectWhereTestFieldExists(null);
    assertEquals(testObject.fieldTwo, found.fieldTwo);
  }

  @Test
  public void testDoesNotExistSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.fieldTwo = 5;
    getDal(Dal.class).create(testObject);
    TestObject found = getDal(Dal.class).findObjectWhereTestFieldDoesNotExist(null);
    assertEquals(testObject.fieldTwo, found.fieldTwo);
  }

  public static class TestObject {
    public String fieldOne;
    public int fieldTwo;
  }

  public interface Dal {
    @Find
    TestObject findObjectWhereTestFieldExists(@Field("fieldOne") @Exists Void na);

    @Find
    TestObject findObjectWhereTestFieldDoesNotExist(@Field("fieldOne") @DoesNotExist Void na);

    @Create
    TestObject create(TestObject testObject);
  }
}
