package com.metapx.local_repo_server.picture_repo;

import java.io.File;
import java.util.stream.Stream;

import com.metapx.local_picture_repo.ResolvedFile;
import com.metapx.local_repo_server.Endpoint;

import io.vertx.core.http.HttpMethod;
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
  }

  @Override
  public void destroy() {
  }

  protected void readImage(RoutingContext routingContext) {
    repoContext.getPictureRepository()
    .subscribe(
      (repo) -> {
        final String hash = routingContext.request().getParam("id");
        final Stream<ResolvedFile> findFiles = repo.findFiles(hash);
        
        routingContext.response().end(hash);
      });
  }
}
