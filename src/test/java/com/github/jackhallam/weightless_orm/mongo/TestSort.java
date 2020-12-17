package com.github.jackhallam.weightless_orm.mongo;

import com.github.jackhallam.weightless_orm.Weightless;
import com.github.jackhallam.weightless_orm.WeightlessORMBuilder;
import com.github.jackhallam.weightless_orm.annotations.Create;
import com.github.jackhallam.weightless_orm.annotations.Field;
import com.github.jackhallam.weightless_orm.annotations.Find;
import com.github.jackhallam.weightless_orm.annotations.Sort;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Equals;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.junit.Assert.assertEquals;

public class TestSort {

  public Weightless weightless;
  public Dal dal;

  @Before
  public void before() throws Exception {
    MongoClient mongoClient = getFakeMongoClient();
    weightless = WeightlessORMBuilder.mongo().client(mongoClient).build();
    dal = weightless.get(Dal.class);
  }

  @After
  public void after() throws Exception {
    if (weightless != null) {
      weightless.close();
    }
  }

  @Test
  public void testSingleSortSuccess() throws Exception {
    TestObject firstTestObject = new TestObject();
    firstTestObject.testField = "abc";
    firstTestObject.secondTestField = 3;
    dal.create(firstTestObject);
    TestObject secondTestObject = new TestObject();
    secondTestObject.testField = "abc";
    secondTestObject.secondTestField = 2;
    secondTestObject = dal.create(secondTestObject);
    TestObject found = dal.findSortBySecondTestField("abc");

    assertEquals(secondTestObject.id, found.id);
    assertEquals(secondTestObject.testField, found.testField);
    assertEquals(secondTestObject.secondTestField, found.secondTestField);
  }

  @Test
  public void testSingleSortDescendingSuccess() throws Exception {
    TestObject firstTestObject = new TestObject();
    firstTestObject.testField = "abc";
    firstTestObject.secondTestField = 2;
    dal.create(firstTestObject);
    TestObject secondTestObject = new TestObject();
    secondTestObject.testField = "abc";
    secondTestObject.secondTestField = 3;
    secondTestObject = dal.create(secondTestObject);
    TestObject found = dal.findSortBySecondTestFieldDescending("abc");

    assertEquals(secondTestObject.id, found.id);
    assertEquals(secondTestObject.testField, found.testField);
    assertEquals(secondTestObject.secondTestField, found.secondTestField);
  }

  @Test
  public void testMultipleSortSuccess() throws Exception {
    TestObject firstTestObject = new TestObject();
    firstTestObject.testField = "abc";
    firstTestObject.secondTestField = 1;
    firstTestObject.thirdTestField = 100;
    dal.create(firstTestObject);
    TestObject secondTestObject = new TestObject();
    secondTestObject.testField = "abc";
    secondTestObject.secondTestField = 2;
    secondTestObject.thirdTestField = 0;
    TestObject thirdTestObject = new TestObject();
    thirdTestObject.testField = "abc";
    thirdTestObject.secondTestField = 1;
    thirdTestObject.thirdTestField = 99;
    thirdTestObject = dal.create(thirdTestObject);
    TestObject found = dal.findSortBySecondAndThirdTestFields("abc");

    assertEquals(thirdTestObject.id, found.id);
    assertEquals(thirdTestObject.testField, found.testField);
    assertEquals(thirdTestObject.secondTestField, found.secondTestField);
  }

  public static class TestObject {
    @Id
    public ObjectId id;
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

  private MongoClient getFakeMongoClient() {
    MongoServer mongoServer = new MongoServer(new MemoryBackend());
    InetSocketAddress serverAddress = mongoServer.bind();
    return new MongoClient(new ServerAddress(serverAddress));
  }
}
