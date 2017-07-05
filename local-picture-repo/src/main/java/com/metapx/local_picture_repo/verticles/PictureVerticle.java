package com.metapx.local_picture_repo.verticles;

import java.util.Optional;

import com.metapx.local_picture_repo.scaling.Dimension;
import com.metapx.local_picture_repo.scaling.FileWithHash;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.RxHelper;
import rx.Single;

/**
 * Given a file hash, returns a path to the file with that hash or to a scaled version of it.
 *
 */
public class PictureVerticle extends AbstractVerticle {
  public static final String FILEHASH_TO_PATH = "picture-verticle.filehash-to-path";

  private PictureRepositoryContext context;
  
  @Override
  public void start() throws Exception {
    context = new PictureRepositoryContext(vertx);
    
    /**
     * Given a file hash, returns a path to the file with that hash or to a scaled version of it.
     */
    vertx.eventBus().<JsonObject>localConsumer(FILEHASH_TO_PATH, message -> {
      final JsonObject body = message.body();
      final String fileHash = body.getString("hash");
      final Optional<Dimension> dim = body.containsKey("dimension") ?
        Optional.of(Dimension.fromJson(message.body().getJsonObject("dimension")))
        : Optional.empty();
      
      context.getPictureRepository()
        .map(repo -> repo.findValidFile(fileHash))
        .subscribeOn(RxHelper.scheduler(vertx))
        .<String>flatMap(file -> {
          if (file.isPresent()) {
            if (dim.isPresent()) {
              return vertx.eventBus().<JsonObject>rxSend(
                  "scaling.verticle",
                  new JsonObject()
                    .put("file", FileWithHash.toJson(file.get()))
                    .put("dimension", dim.get().toJson())
                )
                .map(reply -> reply.body().getString("file"));
            } else {
              return Single.just(file.get().getFile().getAbsolutePath());
            }
          } else {
            throw new RuntimeException("File not found");
          }
        })
        .subscribe(
          (file) -> message.reply(file),
          (error) -> message.fail(1, error.getMessage())
        );
    });
  }

  @Override
  public void stop() throws Exception {
    context.close();
  }
}
