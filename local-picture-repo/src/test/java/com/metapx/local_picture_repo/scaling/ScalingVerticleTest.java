package com.metapx.local_picture_repo.scaling;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import com.metapx.local_picture_repo.impl.DiskFileInformation;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.Message;

public class ScalingVerticleTest {
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  Vertx vertx;
  
  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    
    final JsonObject config = new JsonObject()
      .put("storage", new JsonObject()
        .put("", folder.getRoot().getAbsolutePath())
      );
    vertx.deployVerticle(ScalingVerticle.class.getName(), new DeploymentOptions().setConfig(config));
  }
  
  @After
  public void tearDown() {
    vertx.close();
  }
  
  @Test
  public void testScaling() throws Exception {
    final FileWithHash input = new SampleResolvedFile("IMG_4395.JPG");
    
    final Message<JsonObject> result = vertx.eventBus()
      .<JsonObject>rxSend("scaling.verticle", new JsonObject()
        .put("file", FileWithHash.toJson(input))
        .put("dimension", Dimensions.MEDIUM.toJson())
      )
      .toBlocking()
      .value();

    final File resultFile = new File(result.body().getString("file"));
    final DiskFileInformation resultImage = new DiskFileInformation(resultFile);

    Assert.assertTrue(resultImage.isImage());
    Assert.assertEquals(640, resultImage.getWidth());
    Assert.assertEquals(427, resultImage.getHeight());
  }
  
  @Test
  public void testMissingFile() throws Exception {
    final FileWithHash input = new FileWithHash.Impl(new File("missing-file"), "abcdefghihk");

    thrown.expect(ReplyException.class);
    thrown.expectMessage("Can't read input file!");
    vertx.eventBus()
      .<JsonObject>rxSend("scaling.verticle", new JsonObject()
        .put("file", FileWithHash.toJson(input))
        .put("dimension", Dimensions.MEDIUM.toJson())
      )
      .toBlocking()
      .value();
  }

}
