package com.metapx.local_repo_server.picture_repo;

import java.io.File;
import java.util.Optional;

import com.metapx.local_picture_repo.ResolvedFile;
import com.metapx.local_picture_repo.scaling.Dimension;
import com.metapx.local_repo_server.Endpoint;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;

public class PictureRepositoryEndpoint extends Endpoint {
  final PictureRepositoryContext repoContext;
  
  public PictureRepositoryEndpoint(Vertx vertx) {
    super(vertx);
    repoContext = new PictureRepositoryContext(vertx, new File("C:/Users/Piotrek/.metapx"));
  }

  @Override
  public void register(Router router) {
    router.route(HttpMethod.GET, "/image/:id").blockingHandler(this::readImage);
    router.route(HttpMethod.GET, "/file/:id").blockingHandler(this::readFile);
    router.route(HttpMethod.HEAD, "/file/:id").blockingHandler(this::headFile);
  }

  @Override
  public void destroy() {
    repoContext.close();
  }

  protected void readImage(RoutingContext routingContext) {
    repoContext.getPictureRepository()
    .subscribe(
      (repo) -> {
        final String hash = routingContext.request().getParam("id");
        final Optional<ResolvedFile> resolvedFile = repo.findValidFile(hash);
        
        if (resolvedFile.isPresent()) {
          final JsonObject result = new JsonObject();
          final JsonObject images = new JsonObject();
          result.put("self", routingContext.request().uri());
          result.put("images", images);
          
          images.put("original", getImageOriginal(resolvedFile.get()));

          routingContext.response()
            .putHeader("content-type", "application/json")
            .end(result.encodePrettily());
        } else {
          routingContext.response().setStatusCode(404).end();
        }
      });
  }
  
  protected void readFile(RoutingContext routingContext) {
    repoContext.getPictureRepository()
    .subscribe(
      (repo) -> {
        final String hash = routingContext.request().getParam("id");
        final Optional<ResolvedFile> resolvedFileOpt = repo.findValidFile(hash);
        
        if (resolvedFileOpt.isPresent()) {
          final ResolvedFile resolvedFile = resolvedFileOpt.get();

          routingContext.response()
            .putHeader("content-type", getMimeType(resolvedFile.getImageType()))
            .putHeader("cache-control", "max-age=31556926") // Expires in 1 year
            .sendFile(resolvedFile.getFile().getAbsolutePath())
            .end();
        } else {
          routingContext.response().setStatusCode(404).end();
        }
      });
  }

  protected void headFile(RoutingContext routingContext) {
    repoContext.getPictureRepository()
    .subscribe(
      (repo) -> {
        final String hash = routingContext.request().getParam("id");
        final Optional<ResolvedFile> resolvedFileOpt = repo.findValidFile(hash);
        
        if (resolvedFileOpt.isPresent()) {
          final ResolvedFile resolvedFile = resolvedFileOpt.get();

          routingContext.response()
            .putHeader("content-type", getMimeType(resolvedFile.getImageType()))
            .putHeader("cache-control", "max-age=31556926") // Expires in 1 year
            .end();
        } else {
          routingContext.response().setStatusCode(404).end();
        }
      });
  }

  
  private JsonObject getImageOriginal(ResolvedFile file) {
    final JsonObject result = new JsonObject();
    
    result.put("link", "/file/" + file.getHash());
    result.put("width", file.getWidth());
    result.put("height", file.getHeight());
    result.put("size", file.getFile().length());
    result.put("type", getMimeType(file.getImageType()));
    
    return result;
  }
  
  private JsonObject getImageScaled(ResolvedFile file, Dimension dim) {
    // TODO
    return null;
  }
  
  private String getMimeType(String filetype) {
    switch (filetype) {
    case "JPEG":
      return "image/jpeg";
    default:
      return "application/octet-stream";
    }
  }
}
