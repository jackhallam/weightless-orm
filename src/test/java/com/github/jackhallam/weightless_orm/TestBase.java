package com.github.jackhallam.weightless_orm;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

@RunWith(Parameterized.class)
@Ignore
public class TestBase {

  public Supplier<Weightless> weightlessSupplier;
  private Weightless weightless;
  private static MongoServer mongoServer;
  private static MongoClient mongoClient;

  public TestBase(Supplier<Weightless> weightlessSupplier) {
    this.weightlessSupplier = weightlessSupplier;
  }

  @Before
  public void before() throws Exception {
    weightless = weightlessSupplier.get();
  }

  @After
  public void after() throws Exception {
    if (weightless != null) {
      weightless.close();
    }
    if (mongoServer != null) {
      mongoServer.shutdownNow();
    }
    if (mongoClient != null) {
      mongoClient.close();
    }
  }

  public <T> T getDal(Class<T> clazz) {
    return weightless.get(clazz);
  }

  @Parameterized.Parameters
  public static Collection<Supplier[]> data() {
    Supplier[][] data = {
      {
        (Supplier<Weightless>) () -> Weightless.inMemory().build()
      },
      {
        (Supplier<Weightless>) () -> {
          mongoServer = new MongoServer(new MemoryBackend());
          InetSocketAddress serverAddress = mongoServer.bind();
          mongoClient = new MongoClient(new ServerAddress(serverAddress));
          return Weightless.mongo().client(mongoClient).build();
        }
      }
    };
    return Arrays.asList(data);
  }
}
