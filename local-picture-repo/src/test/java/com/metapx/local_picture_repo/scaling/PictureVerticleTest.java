package com.metapx.local_picture_repo.scaling;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import com.metapx.local_picture_repo.SamplePictures;
import com.metapx.local_picture_repo.database.ConnectionFactory;
import com.metapx.local_picture_repo.impl.DiskFileInformation;
import com.metapx.local_picture_repo.impl.RepositoryImpl;
import com.metapx.local_picture_repo.verticles.PictureVerticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.Message;
import rx.Observable;
import rx.Single;

public class PictureVerticleTest {
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  Vertx vertx;

  public static ArrayList<DiskFileInformation> files = new ArrayList<DiskFileInformation>();
  
  @BeforeClass
  public static void setupAll() throws Exception {
    ConnectionFactory.SharedConnectionPool.create(() -> ConnectionFactory.newInMemoryConnectionPool());
    
    try(final Connection conn = ConnectionFactory.SharedConnectionPool.getConnectionPool().getConnection()) {
      final RepositoryImpl repo = new RepositoryImpl(conn);
      files.add(new DiskFileInformation(SamplePictures.getFile("IMG_3847.JPG")));
      files.add(new DiskFileInformation(SamplePictures.getFile("IMG_4395.JPG")));
      files.add(new DiskFileInformation(SamplePictures.getFile("IMG_4399.JPG")));
      files.add(new DiskFileInformation(SamplePictures.getFile("IMG_4970.JPG")));
      for(final DiskFileInformation file : files) repo.addFile(file);
    }
  }
  
  @Before
  public void setUp() throws Exception {
    vertx = Vertx.vertx();
    
    final JsonObject config = new JsonObject()
      .put("storage", new JsonObject()
        .put("", folder.getRoot().getAbsolutePath())
      );
    vertx.deployVerticle(ScalingVerticle.class.getName(), new DeploymentOptions().setConfig(config));
    vertx.deployVerticle(PictureVerticle.class.getName());
  }
  
  @After
  public void tearDown() {
    vertx.close();
  }
  
  @Test
  public void testScaling() throws Exception {
    final Message<String> result = vertx.eventBus()
      .<String>rxSend("picture-verticle.filehash-to-path", new JsonObject()
        .put("hash", files.get(0).getHash())
        .put("dimension", Dimensions.MEDIUM.toJson())
      )
      .toBlocking()
      .value();

    final File resultFile = new File(result.body());
    final DiskFileInformation resultImage = new DiskFileInformation(resultFile);

    Assert.assertTrue(resultImage.isImage());
    Assert.assertEquals(640, resultImage.getWidth());
    Assert.assertEquals(427, resultImage.getHeight());
  }

  @Test
  public void testConcurrentScaling() throws Exception {
    final Single<Message<String>> stream1 = vertx.eventBus()
      .<String>rxSend("picture-verticle.filehash-to-path", new JsonObject()
        .put("hash", files.get(0).getHash())
        .put("dimension", Dimensions.LARGE.toJson())
      );
    final Single<Message<String>> stream2 = vertx.eventBus()
      .<String>rxSend("picture-verticle.filehash-to-path", new JsonObject()
        .put("hash", files.get(1).getHash())
        .put("dimension", Dimensions.MEDIUM.toJson())
      );
    
    @SuppressWarnings("unchecked")
    final Message<String>[] result = Observable.combineLatest(stream1.toObservable(), stream2.toObservable(), (a,b) -> new Message[] { a, b })
      .toBlocking()
      .first();

    final DiskFileInformation resultImage1 = new DiskFileInformation(new File(result[0].body()));
    final DiskFileInformation resultImage2 = new DiskFileInformation(new File(result[1].body()));

    Assert.assertTrue(resultImage1.isImage());
    Assert.assertTrue(resultImage2.isImage());
    Assert.assertEquals(1024, resultImage1.getWidth());
    Assert.assertEquals(683, resultImage1.getHeight());
    Assert.assertEquals(640, resultImage2.getWidth());
    Assert.assertEquals(427, resultImage2.getHeight());
  }

  @Test
  public void testOriginal() throws Exception {
    final Message<String> result = vertx.eventBus()
      .<String>rxSend("picture-verticle.filehash-to-path", new JsonObject()
        .put("hash", files.get(0).getHash())
      )
      .toBlocking()
      .value();

    final File resultFile = new File(result.body());
    final DiskFileInformation resultImage = new DiskFileInformation(resultFile);

    Assert.assertTrue(resultImage.isImage());
    Assert.assertEquals(5184, resultImage.getWidth());
    Assert.assertEquals(3456, resultImage.getHeight());
  }

  
}
