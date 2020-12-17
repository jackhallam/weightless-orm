package com.github.jackhallam.weightless_orm.mongo;


import com.github.jackhallam.weightless_orm.Weightless;
import com.github.jackhallam.weightless_orm.WeightlessORMBuilder;
import com.github.jackhallam.weightless_orm.annotations.Create;
import com.github.jackhallam.weightless_orm.annotations.Field;
import com.github.jackhallam.weightless_orm.annotations.Find;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestFindAndCreate {

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
  public void testFindEmptySuccess() throws Exception {
    TestObject testObject = dal.find("non_existent");

    assertNull(testObject);
  }

  @Test
  public void testFindAllEmptySuccess() throws Exception {
    List<TestObject> testObjects = dal.findAll();

    assertTrue(testObjects.isEmpty());
  }

  @Test
  public void testCreateOneFindOneSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    TestObject savedTestObject = dal.create(testObject);
    TestObject foundTestObject = dal.find("hello");

    assertEquals(savedTestObject.testField, foundTestObject.testField);
  }

  @Test
  public void testCreateOneFindAllSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    testObject = dal.create(testObject);
    List<TestObject> foundTestObjects = dal.findAll();

    assertEquals(1, foundTestObjects.size());
    assertEquals(testObject.testField, foundTestObjects.get(0).testField);
  }

  @Test
  public void testCreateIdCreatedSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "hello";
    TestObject savedTestObject = dal.create(testObject);
    TestObject foundTestObject = dal.find("hello");

    assertNotNull(foundTestObject.id);
    assertEquals(savedTestObject.id, foundTestObject.id);
  }

  public static class TestObject {
    @Id
    public ObjectId id;
    public String testField;
  }

  public interface Dal {
    @Find
    TestObject find(@Field("testField") @Equals String testField);

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
