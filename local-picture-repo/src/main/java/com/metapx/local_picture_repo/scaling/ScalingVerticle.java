package com.metapx.local_picture_repo.scaling;

import java.io.File;
import java.io.IOException;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.WorkerExecutor;

public class ScalingVerticle extends AbstractVerticle {
  ScaledPictureMultiProvider provider;
  
  @Override
  public void start() throws Exception {
    provider = new ScaledPictureMultiProvider();
    configureMultiProvider();

    WorkerExecutor executor = vertx.createSharedWorkerExecutor("my-worker-pool");
    
    vertx.eventBus().<JsonObject>localConsumer("scaling.verticle", message -> {
      final FileWithHash file = FileWithHash.fromJson(message.body().getJsonObject("file"));
      final Dimension dim = Dimension.fromJson(message.body().getJsonObject("dimension"));
      
      executor.<File>rxExecuteBlocking(
        future -> {
          try {
            future.complete(provider.getScaledImage(file, dim));
          } catch (IOException | InterruptedException e) {
            future.fail(e);
          }
        },
        false)
        .subscribe(
          resultFile -> {
            message.reply(new JsonObject()
              .put("file", resultFile.getAbsolutePath())
              );
          },
          error -> message.fail(1, error.getMessage())
        );
    });
  }
  
  private void configureMultiProvider() {
    config().getJsonObject("storage").forEach(pair -> {
      provider.addProvider(new File((String) pair.getValue()), pair.getKey());
    });
  }
}
