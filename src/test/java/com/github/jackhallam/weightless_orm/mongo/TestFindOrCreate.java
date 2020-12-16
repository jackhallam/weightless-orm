package com.github.jackhallam.weightless_orm.mongo;

import com.github.jackhallam.weightless_orm.Weightless;
import com.github.jackhallam.weightless_orm.WeightlessORMBuilder;
import com.github.jackhallam.weightless_orm.annotations.*;
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
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class TestFindOrCreate {

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
  public void testFindOrCreateCreatedSuccess() throws Exception {
    TestObject testObject = dal.findOrCreate("the field value");

    assertEquals("the field value", testObject.testField);
  }

  @Test
  public void testFindOrCreateFoundSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "abc";
    testObject = dal.create(testObject);
    TestObject foundTestObject = dal.findOrCreate("abc");

    assertNotNull(foundTestObject.id);
    assertEquals(testObject.id, foundTestObject.id);
  }

  @Test
  public void testFindOrCreateOptionalSuccess() throws Exception {
    Optional<TestObject> testObjectOptional = dal.findOrCreateOptional("the field value");

    assertTrue(testObjectOptional.isPresent());
    assertEquals("the field value", testObjectOptional.get().testField);
  }

  @Test
  public void testFindOrCreateReturnAllSuccess() throws Exception {
    TestObject firstTestObject = new TestObject();
    firstTestObject.testField = "abc";
    dal.create(firstTestObject);
    TestObject secondTestObject = new TestObject();
    secondTestObject.testField = "def";
    dal.create(secondTestObject);
    List<TestObject> objects = dal.findOrCreateReturnAll("abc");

    assertEquals(1, objects.size());
  }

  @Test
  public void testFindOrCreateSortedSuccess() throws Exception {
    TestObject firstTestObject = new TestObject();
    firstTestObject.testField = "abc";
    firstTestObject.otherTestField = 2;
    firstTestObject = dal.create(firstTestObject);
    TestObject secondTestObject = new TestObject();
    secondTestObject.testField = "abc";
    secondTestObject.otherTestField = 1;
    secondTestObject = dal.create(secondTestObject);

    // It finds in this case
    List<TestObject> objects = dal.findOrCreateSorted("abc");

    assertEquals(2, objects.size());
    assertEquals(secondTestObject.id, objects.get(0).id);
    assertEquals(firstTestObject.id, objects.get(1).id);
  }

  public static class TestObject {
    @Id
    ObjectId id;
    String testField;
    int otherTestField;
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

  private MongoClient getFakeMongoClient() {
    MongoServer mongoServer = new MongoServer(new MemoryBackend());
    InetSocketAddress serverAddress = mongoServer.bind();
    return new MongoClient(new ServerAddress(serverAddress));
  }
}
