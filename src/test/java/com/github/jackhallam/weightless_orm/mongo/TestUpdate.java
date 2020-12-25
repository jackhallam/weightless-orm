package com.github.jackhallam.weightless_orm.mongo;

import com.github.jackhallam.weightless_orm.Weightless;
import com.github.jackhallam.weightless_orm.annotations.Create;
import com.github.jackhallam.weightless_orm.annotations.Field;
import com.github.jackhallam.weightless_orm.annotations.Find;
import com.github.jackhallam.weightless_orm.annotations.Update;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Equals;
import org.junit.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

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

  public static class TestObject {
    public String testField;
    public int secondTestField;
  }

  public interface Dal {

    @Find
    List<TestObject> findAll();

    @Update
    TestObject update(TestObject testObject, @Field("testField") @Equals String testField);

    @Create
    TestObject create(TestObject testObject);
  }
}
