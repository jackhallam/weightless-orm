package com.github.jackhallam.weightless_orm.mongo;

import com.github.jackhallam.weightless_orm.Weightless;
import com.github.jackhallam.weightless_orm.WeightlessORMBuilder;
import com.github.jackhallam.weightless_orm.annotations.*;
import com.github.jackhallam.weightless_orm.annotations.field_filters.Exists;
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
import java.util.Optional;

import static org.junit.Assert.*;

public class TestExistence {

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
  public void testExistsSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.testField = "abc";
    testObject.otherTestField = 5;
    dal.create(testObject);
    Optional<TestObject> foundOptional = dal.findObjectWhereTestFieldExists(null);
    assertTrue(foundOptional.isPresent());
    assertEquals(testObject.otherTestField, foundOptional.get().otherTestField);
  }

  @Test
  public void testDoesNotExistSuccess() throws Exception {
    TestObject testObject = new TestObject();
    testObject.otherTestField = 5;
    dal.create(testObject);
    Optional<TestObject> foundOptional = dal.findObjectWhereTestFieldDoesNotExist(null);
    assertFalse(foundOptional.isPresent());
  }

  public static class TestObject {
    @Id
    ObjectId id;
    String testField;
    int otherTestField;
  }

  public interface Dal {
    @Find
    Optional<TestObject> findObjectWhereTestFieldExists(@Field("testField") @Exists Void na);

    @Find
    Optional<TestObject> findObjectWhereTestFieldDoesNotExist(@Field("testField") @Exists Void na);

    @Create
    TestObject create(TestObject testObject);
  }

  private MongoClient getFakeMongoClient() {
    MongoServer mongoServer = new MongoServer(new MemoryBackend());
    InetSocketAddress serverAddress = mongoServer.bind();
    return new MongoClient(new ServerAddress(serverAddress));
  }
}
