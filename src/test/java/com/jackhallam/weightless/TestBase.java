package com.jackhallam.weightless;

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
  }

  public <T> T getDal(Class<T> clazz) {
    return weightless.get(clazz);
  }

  @Parameterized.Parameters
  public static Collection<Supplier[]> data() {
    Supplier[][] data = {
      {
        (Supplier<Weightless>) () -> Weightless.h2Memory().build()
      },
      {
        (Supplier<Weightless>) () -> Weightless.inMemory().build()
      },
      {
        (Supplier<Weightless>) () -> {
          mongoServer = new MongoServer(new MemoryBackend());
          InetSocketAddress serverAddress = mongoServer.bind();
          return Weightless.mongo("mongodb://" + new ServerAddress(serverAddress).toString()).build();
        }
      }
    };
    return Arrays.asList(data);
  }
}
