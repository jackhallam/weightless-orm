package com.github.jackhallam.weightless_orm.mongo;

import com.github.jackhallam.weightless_orm.Weightless;
import com.github.jackhallam.weightless_orm.annotations.Create;
import com.github.jackhallam.weightless_orm.annotations.Field;
import com.github.jackhallam.weightless_orm.annotations.Find;
import com.github.jackhallam.weightless_orm.annotations.field_filters.DoesNotExist;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Exists;
import org.junit.Test;

import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestExistence extends TestBase {

  public TestExistence(Supplier<Weightless> weightlessSupplier) {
    super(weightlessSupplier);
  }

  @Test
  public void testExistsSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "abc";
    testObject.otherTestField = 5;
    getDal(Dal.class).create(testObject);
    Optional<TestObject> foundOptional = getDal(Dal.class).findObjectWhereTestFieldExists(null);
    assertTrue(foundOptional.isPresent());
    assertEquals(testObject.otherTestField, foundOptional.get().otherTestField);
  }

  @Test
  public void testDoesNotExistSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.otherTestField = 5;
    getDal(Dal.class).create(testObject);
    Optional<TestObject> foundOptional = getDal(Dal.class).findObjectWhereTestFieldDoesNotExist(null);
    assertTrue(foundOptional.isPresent());
    assertEquals(testObject.otherTestField, foundOptional.get().otherTestField);
  }

  public static class TestObject {
    public String testField;
    public int otherTestField;
  }

  public interface Dal {
    @Find
    Optional<TestObject> findObjectWhereTestFieldExists(@Field("testField") @Exists Void na);

    @Find
    Optional<TestObject> findObjectWhereTestFieldDoesNotExist(@Field("testField") @DoesNotExist Void na);

    @Create
    TestObject create(TestObject testObject);
  }
}
