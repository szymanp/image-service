package com.metapx.local_repo_server.picture_repo;

import java.io.File;
import java.util.Optional;

import com.metapx.local_picture_repo.ResolvedFile;
import com.metapx.local_picture_repo.FileInformation;
import com.metapx.local_picture_repo.impl.DiskFileInformation;
import com.metapx.local_picture_repo.impl.RepositoryFileInformation;
import com.metapx.local_picture_repo.scaling.Dimension;
import com.metapx.local_picture_repo.scaling.Dimensions;
import com.metapx.local_picture_repo.scaling.ScaledPictureProvider;
import com.metapx.local_picture_repo.scaling.ScaledPictureProvider.Status;
import com.metapx.local_picture_repo.verticles.PictureRepositoryContext;
import com.metapx.local_picture_repo.scaling.ScaledPictureProviderImpl;
import com.metapx.local_repo_server.Endpoint;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;

public class PictureRepositoryEndpoint extends Endpoint {
  final PictureRepositoryContext repoContext;
  final ScaledPictureProvider scaling;
  
  public PictureRepositoryEndpoint(Vertx vertx, File scaledPictureCache) {
    super(vertx);
    repoContext = new PictureRepositoryContext(vertx);
    scaling = new ScaledPictureProviderImpl(scaledPictureCache);
  }

  @Override
  public void register(Router router) {
    router.route(HttpMethod.GET, "/image/:id").blockingHandler(this::readImage);

    router.route(HttpMethod.GET, "/image/:id/:dim").blockingHandler(this::readScaledImageStatus);
    router.route(HttpMethod.POST, "/image/:id/:dim").blockingHandler(this::createScaledImage);
    
    router.route(HttpMethod.GET, "/file/:id/:dim").blockingHandler(this::readFile);
    router.route(HttpMethod.HEAD, "/file/:id/:dim").blockingHandler(this::readFile);
  }

  @Override
  public void destroy() {
    repoContext.close();
  }

  protected void readImage(RoutingContext routingContext) {
    repoContext.getPictureRepository()
    .doOnSuccess(
      (repo) -> {
        final String hash = routingContext.request().getParam("id");
        final Optional<ResolvedFile> resolvedFileOpt = repo.findValidFile(hash);
        
        if (resolvedFileOpt.isPresent()) {
          final JsonObject result = new JsonObject();
          final JsonObject images = new JsonObject();
          final ResolvedFile resolvedFile = resolvedFileOpt.get();

          result.put("self", routingContext.request().uri());
          result.put("images", images);
          
          images.put("original", getImageOriginal(resolvedFile));
          for(Dimension.NamedDimension dim : Dimensions.values()) {
            if (isApplicable(dim, resolvedFile)) {
              images.put(dim.getName(), getImageScaled(resolvedFile, dim));
            }
          }

          routingContext.response()
            .putHeader("content-type", "application/json")
            .end(result.encodePrettily());
        } else {
          routingContext.response().setStatusCode(404).end();
        }
      })
    .doOnError(error -> sendError(routingContext, error))
    .subscribe();
  }
  
  protected void readScaledImageStatus(RoutingContext routingContext) {
    repoContext.getPictureRepository()
    .doOnSuccess(
      (repo) -> {
        final String hash = routingContext.request().getParam("id");
        final Optional<Dimension> dim = getDimension(routingContext.request().getParam("dim"));
        final Optional<ResolvedFile> resolvedFileOpt = repo.findValidFile(hash);
        
        if (resolvedFileOpt.isPresent() && dim.isPresent()) {
          final Dimension.NamedDimension namedDim = (Dimension.NamedDimension) dim.get();
          final Status status = scaling.getScaledImageStatus(resolvedFileOpt.get(), namedDim);
          
          switch (status) {
          case MISSING:
            routingContext.response()
              .setStatusCode(403) // Forbidden
              .end();
            break;
          case IN_PROGRESS:
            final JsonObject result = new JsonObject();
            result.put("retry", 1000);
            
            routingContext.response()
              .setStatusCode(200)
              .end(result.encode());
            break;
          case EXISTS:
            routingContext.response()
              .setStatusCode(303) // See Other
              .putHeader("Location", "/file/" + hash + "/" + namedDim.getName())
              .end();
          }
        } else {
          routingContext.response().setStatusCode(404).end();
        }
      })
    .doOnError(error -> sendError(routingContext, error))
    .subscribe();
  }
  
  protected void createScaledImage(RoutingContext routingContext) {
    repoContext.getPictureRepository()
    .doOnSuccess(
      (repo) -> {
        final String hash = routingContext.request().getParam("id");
        final Optional<Dimension> dim = getDimension(routingContext.request().getParam("dim"));
        final Optional<ResolvedFile> resolvedFileOpt = repo.findValidFile(hash);
        
        if (resolvedFileOpt.isPresent() && dim.isPresent()) {
          routingContext.response()
            .setStatusCode(202) // Accepted
            .putHeader("Location", routingContext.request().uri())
            .end();

          try {
            scaling.getScaledImage(resolvedFileOpt.get(), dim.get());
          } catch (Exception e) {
            // ignore
          }
        } else {
          routingContext.response().setStatusCode(404).end();
        }
      })
    .doOnError(error -> sendError(routingContext, error))
    .subscribe();
  }
  
  protected void readFile(RoutingContext routingContext) {
    repoContext.getPictureRepository()
    .doOnSuccess(
      (repo) -> {
        final String hash    = routingContext.request().getParam("id");
        final String dimName = routingContext.request().getParam("dim");
        final Optional<FileInformation> fileOpt = repo.findValidFile(hash)
          .flatMap(resolvedFile -> {
            if (dimName.equals("original")) {
              return Optional.of(new RepositoryFileInformation(resolvedFile)); 
            } else {
              return getDimension(dimName)
                .flatMap(dim -> scaling.getScaledImageIfExists(resolvedFile, dim))
                .map(file -> new DiskFileInformation(file));
            }
          });
        
        if (fileOpt.isPresent()) {
          final FileInformation file = fileOpt.get();
          final HttpServerResponse response = routingContext.response();

          response
            .putHeader("content-type", getMimeType(file.getImageType()))
            .putHeader("cache-control", "max-age=31556926"); // Expires in 1 year
          
          if (routingContext.request().method() == HttpMethod.GET) {
            response.sendFile(file.getFile().getAbsolutePath());
          }
          
          response.end();
        } else {
          routingContext.response().setStatusCode(404).end();
        }
      })
    .doOnError(error -> sendError(routingContext, error))
    .subscribe();
  }
  
  private void sendError(RoutingContext routingContext, Throwable error) {
    error.printStackTrace();

    routingContext.response()
      .setStatusCode(500)
      .end();
  }
  
  private JsonObject getImageOriginal(ResolvedFile file) {
    final JsonObject result = new JsonObject();
    
    result.put("link", "/file/" + file.getHash() + "/original");
    result.put("width", file.getWidth());
    result.put("height", file.getHeight());
    result.put("size", file.getFile().length());
    result.put("type", getMimeType(file.getImageType()));
    
    return result;
  }
  
  private JsonObject getImageScaled(ResolvedFile original, Dimension.NamedDimension dim) {
    final JsonObject result = new JsonObject();
    final Optional<File> scaledImageOpt = scaling.getScaledImageIfExists(original, dim);
    
    if (scaledImageOpt.isPresent()) {
      final DiskFileInformation scaledFile = new DiskFileInformation(scaledImageOpt.get());
      result.put("link", "/file/" + original.getHash() + "/" + dim.getName());
      result.put("width", scaledFile.getWidth());
      result.put("height", scaledFile.getHeight());
      result.put("size", scaledFile.getFile().length());
      result.put("type", getMimeType(scaledFile.getImageType()));
    } else {
      result.put("create", "/image/" + original.getHash() + "/" + dim.getName());
      result.put("approximateWidth", dim.getWidth());
      result.put("approximateHeight", dim.getHeight());
    }
    
    return result;
  }
  
  private Optional<Dimension> getDimension(String name) {
    try {
      return Optional.of(Dimensions.valueOf(name.toUpperCase()));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }
  
  private String getMimeType(String filetype) {
    switch (filetype) {
    case "JPEG":
      return "image/jpeg";
    default:
      return "application/octet-stream";
    }
  }
  
  private boolean isApplicable(Dimension dim, ResolvedFile original) {
    return original.getWidth() - dim.getWidth() > 100
      && original.getHeight() - dim.getHeight() > 100;
  }
}
