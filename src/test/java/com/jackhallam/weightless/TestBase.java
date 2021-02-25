package com.jackhallam.weightless;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import com.mongodb.ServerAddress;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

@RunWith(Parameterized.class)
@Ignore
public class TestBase {

  public Supplier<Weightless> weightlessSupplier;
  private Weightless weightless;
  private static MongoServer mongoServer;
  private static DB mySqlDb;
  private static MySQLContainer<?> mySqlContainer;

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
    if (mySqlDb != null) {
      mySqlDb.stop();
    }
    if (mySqlContainer != null) {
      mySqlContainer.stop();
    }
  }

  public <T> T getDal(Class<T> clazz) {
    return weightless.get(clazz);
  }

  @Parameterized.Parameters
  public static Collection<Supplier[]> data() {
    Supplier[][] data = {
      {
        (Supplier<Weightless>) () -> Weightless.h2().memoryBacked().build()
      },
      {
        (Supplier<Weightless>) () -> {
          try {
            File file = File.createTempFile("temp", null);
            file.deleteOnExit();
            Path path = file.toPath();
            return Weightless.h2().fileBacked(path).build();
          } catch (IOException ignored) {
            return null;
          }
        }
      },
      {
        (Supplier<Weightless>) () -> {
          mySqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql"));
          mySqlContainer.start();
          return Weightless.mySql().jdbcUrl("jdbc:tc:mysql:///test?user=root&password=").database("temp").build();
        }
      },
      {
        (Supplier<Weightless>) () -> {
          try {
            File file = Files.createTempDirectory("temp").toFile();
            file.deleteOnExit();
            Path path = file.toPath();
            DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
            configBuilder.setPort(0);
            configBuilder.setDataDir(path.toString());
            mySqlDb = DB.newEmbeddedDB(configBuilder.build());
            mySqlDb.start();
            return Weightless.mySql().host("localhost:" + mySqlDb.getConfiguration().getPort()).database("temp").build();
          } catch (ManagedProcessException | IOException ignored) {
            return null;
          }
        }
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
