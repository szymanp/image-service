package com.metapx.local_repo_server;

import com.metapx.local_repo_server.metadata_repo.MetadataRepositoryEndpoint;
import com.metapx.local_repo_server.util.HttpStatusError;

import io.vertx.core.http.HttpServerOptions;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.ext.web.Router;

public class LocalServerVerticle extends AbstractVerticle {
  private Endpoint[] endpoints;
  
  @Override
  public void start() throws Exception {
    super.start();
    
    final HttpServerOptions options = new HttpServerOptions();
    final HttpServer httpServer = vertx.createHttpServer(options);
    final Router router = Router.router(vertx);
    
    endpoints = new Endpoint[] { new MetadataRepositoryEndpoint(vertx) };
    for(Endpoint endpoint : endpoints) {
      endpoint.register(router);
    }
    
    router.route().failureHandler(routingContext -> {
      HttpStatusError.endWithError(routingContext.failure(), routingContext.response());
    });
    
    httpServer.requestHandler(router::accept).listen(config().getInteger("http.port", 8081), "127.0.0.1");
  }
  
  @Override
  public void stop() throws Exception {
    super.stop();

    for(Endpoint endpoint : endpoints) {
      endpoint.destroy();
    }
  }
}
