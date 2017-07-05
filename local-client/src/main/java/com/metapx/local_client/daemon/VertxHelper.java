package com.metapx.local_client.daemon;

import java.io.File;

import com.metapx.local_client.cli.Configuration;
import com.metapx.local_picture_repo.scaling.ScalingVerticle;
import com.metapx.local_picture_repo.verticles.PictureVerticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;

public class VertxHelper {
  private final Vertx vertx;
  private final Configuration conf;

  public VertxHelper(Vertx vertx, Configuration conf) {
    this.vertx = vertx;
    this.conf = conf;
  }
  
  public VertxHelper(Configuration conf) {
    this(Vertx.vertx(), conf);
  }
  
  public VertxHelper() {
    this(Vertx.vertx(), Configuration.getDefaultConfiguration());
  }

  public Vertx vertx() {
    return vertx;
  }
  
  public void deployScalingVerticle() {
    final JsonObject storage = new JsonObject()
      .put("", conf.getConfigurationDirectory().getAbsolutePath() + File.separator + "scaled-cache");
    
    final JsonObject config = new JsonObject()
      .put("storage", storage);
    
    vertx.deployVerticle(ScalingVerticle.class.getName(), new DeploymentOptions().setConfig(config));
  }
  
  public void deployPictureVerticle() {
    vertx.deployVerticle(PictureVerticle.class.getName());
  }
}
