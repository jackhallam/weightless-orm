package com.github.jackhallam.weightless_orm.mongo;

import com.github.jackhallam.weightless_orm.Weightless;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestMongoOrm {

  public static String BD_NAME = "FAKE_MONGO_DB";

  public MongoClient mongoClient;
  public Datastore datastore;

  public Weightless weightless;
  public PersonDal personDal;

  @Before
  public void before() throws Exception {
    mongoClient = getFakeMongoClient();
    Morphia morphia = new Morphia();
    morphia.mapPackage("com.github.jackhallam.weightless_orm.mongo");
    datastore = morphia.createDatastore(mongoClient, BD_NAME);

    weightless = new Weightless(datastore);
    personDal = weightless.get(PersonDal.class);
  }

  @After
  public void after() throws Exception {
    if (mongoClient != null) {
      mongoClient.close();
    }
  }

  @Test
  public void testAddSuccess() throws Exception {
    addPerson(1, "Blue", 2);

    List<Person> people = personDal.getAllPeople();
    assertEquals(1, people.size());
    assertEquals(1, people.get(0).id);
    assertEquals("Blue", people.get(0).favoriteColor);
    assertEquals(2, people.get(0).favoriteNumber);
  }

  @Test
  public void testUpdateSuccess() throws Exception {
    addPerson(1, "Blue", 2);

    Person updatedPerson = personDal.getPerson(1).get();
    updatedPerson.favoriteNumber = 3;
    personDal.updatePerson(updatedPerson);

    List<Person> people = personDal.getAllPeople();
    assertEquals(1, people.size());
    assertEquals(1, people.get(0).id);
    assertEquals("Blue", people.get(0).favoriteColor);
    assertEquals(3, people.get(0).favoriteNumber);
  }

  @Test
  public void testSingleFieldFindSuccess() throws Exception {
    Person personOne = addPerson(1, "Blue", 2);
    Person personTwo = addPerson(2, "Red", 3);
    Person personThree = addPerson(3, "Green", 2);

    List<Person> people = personDal.peopleWithFavoriteNumber(2);
    assertEquals(2, people.size());
  }

  @Test
  public void testMultipleFieldFindSuccess() throws Exception {
    Person personOne = addPerson(1, "Blue", 2);
    Person personTwo = addPerson(2, "Red", 5);
    Person personThree = addPerson(3, "Green", 10);

    List<Person> people = personDal.peopleWithFavoriteNumberBetween(4,6);
    assertEquals(1, people.size());
  }

  @Test
  public void testAndFnFindSuccess() throws Exception {
    Person personOne = addPerson(1, "Blue", 2);
    Person personTwo = addPerson(2, "Red", 5);
    Person personThree = addPerson(3, "Green", 10);

    List<Person> people = personDal.peopleWithFavoriteColorAndFavoriteNumberBetween("Red", 4, 6);
    assertEquals(1, people.size());
  }

  @Test
  public void testOrFnFindSuccess() throws Exception {
    Person personOne = addPerson(1, "Blue", 2);
    Person personTwo = addPerson(2, "Red", 3);
    Person personThree = addPerson(3, "Green", 10);

    List<Person> people = personDal.peopleWithFavoriteColorOrFavoriteNumberBetween("Red", 9, 11);
    assertEquals(2, people.size());
  }

  @Test
  public void testSingleSortSuccess() throws Exception {
    Person personOne = addPerson(1, "Blue", 100);
    Person personTwo = addPerson(2, "Red", 1);
    Person personThree = addPerson(3, "Green", 50);
    Person personFour = addPerson(4, "Orange", 51);

    List<Person> people = personDal.peopleByLowestFavoriteNumber();
    assertEquals(4, people.size());
    assertEquals(2, people.get(0).id);
    assertEquals(3, people.get(1).id);
    assertEquals(4, people.get(2).id);
    assertEquals(1, people.get(3).id);
  }

  @Test
  public void testMultipleSortSuccess() throws Exception {
    Person personOne = addPerson(1, "Blue", 20);
    Person personTwo = addPerson(2, "Red", 100);
    Person personThree = addPerson(3, "Green", 20);
    Person personFour = addPerson(4, "Orange", 100);

    List<Person> people = personDal.peopleByFavoriteNumberAndHighestId();
    assertEquals(4, people.size());
    assertEquals(3, people.get(0).id);
    assertEquals(1, people.get(1).id);
    assertEquals(4, people.get(2).id);
    assertEquals(2, people.get(3).id);
  }

  private Person addPerson(int id, String color, int number) {
    Person personThree = new Person();
    personThree.id = id;
    personThree.favoriteColor = color;
    personThree.favoriteNumber = number;
    return personDal.addPerson(personThree);
  }

  private MongoClient getFakeMongoClient() {
    MongoServer mongoServer = new MongoServer(new MemoryBackend());
    InetSocketAddress serverAddress = mongoServer.bind();
    return new MongoClient(new ServerAddress(serverAddress));
  }
}
