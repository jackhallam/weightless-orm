package com.github.jackhallam.weightless_orm.mongo;

import com.github.jackhallam.weightless_orm.Weightless;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestFindOrCreate {

  public static String DB_NAME = "FAKE_MONGO_DB";

  public MongoClient mongoClient;
  public Weightless weightless;
  public TestObjectDal testObjectDal;

  @Before
  public void before() throws Exception {
    mongoClient = getFakeMongoClient();
    weightless = new Weightless(mongoClient, DB_NAME);
    testObjectDal = weightless.get(TestObjectDal.class);
  }

  @After
  public void after() throws Exception {
    if (mongoClient != null) {
      mongoClient.close();
    }
  }

  @Test
  public void testFindOrCreateSuccess() throws Exception {
    TestObject testObject = testObjectDal.findOrCreatePojo("the field value");
    assertEquals("the field value", testObject.testField);
  }

  @Test
  public void testFindOrCreateReturnAllSuccess() throws Exception {
    TestObject firstTestObject = new TestObject();
    firstTestObject.testField = "abc";
    firstTestObject = testObjectDal.create(firstTestObject);
    TestObject secondTestObject = new TestObject();
    secondTestObject.testField = "def";
    secondTestObject = testObjectDal.create(secondTestObject);
    List<TestObject> objects = testObjectDal.findOrCreateReturnAll("abc");

    assertEquals(1, objects.size());
  }

  @Test
  public void testFindOrCreateSortedSuccess() throws Exception {
    TestObject firstTestObject = new TestObject();
    firstTestObject.testField = "abc";
    firstTestObject.otherTestField = 2;
    firstTestObject = testObjectDal.create(firstTestObject);
    TestObject secondTestObject = new TestObject();
    secondTestObject.testField = "abc";
    secondTestObject.otherTestField = 1;
    secondTestObject = testObjectDal.create(secondTestObject);

    // It finds in this case
    List<TestObject> objects = testObjectDal.findOrCreateSorted("abc");

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

  @Dal
  public interface TestObjectDal {
    @FindOrCreate
    TestObject findOrCreatePojo(@Field("testField") @Equals String testField);

    @FindOrCreate
    Optional<TestObject> findOrCreateOptional(@Field("testField") @Equals String testField);

    @FindOrCreate
    List<TestObject> findOrCreateReturnAll(@Field("testField") @Equals String testField);

    @FindOrCreate
    @Sort(onField = "otherTestField")
    List<TestObject> findOrCreateSorted(@Field("testField") @Equals String testField);

    @Find
    List<TestObject> findAll();

    @Create
    TestObject create(TestObject testObject);
  }

  private MongoClient getFakeMongoClient() {
    MongoServer mongoServer = new MongoServer(new MemoryBackend());
    InetSocketAddress serverAddress = mongoServer.bind();
    return new MongoClient(new ServerAddress(serverAddress));
  }
}
