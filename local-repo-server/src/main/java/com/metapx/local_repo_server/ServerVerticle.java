package com.metapx.local_repo_server;

import com.metapx.local_repo_server.picture_repo.PictureRepositoryEndpoint;

import io.vertx.core.http.HttpServerOptions;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.ext.web.Router;

public class ServerVerticle extends AbstractVerticle {
  
  private Endpoint[] endpoints;
  
  @Override
  public void start() throws Exception {
    super.start();
    
    final HttpServerOptions options = new HttpServerOptions();
    final HttpServer httpServer = vertx.createHttpServer(options);
    final Router router = Router.router(vertx);
    
    router.route("/hello").handler(routingContext -> {
      routingContext.response().end("Hello world");
    });
    
    endpoints = new Endpoint[] { new PictureRepositoryEndpoint(vertx) };
    for(Endpoint endpoint : endpoints) {
      endpoint.register(router);
    }
    
    httpServer.requestHandler(router::accept).listen(config().getInteger("http.port", 8080));
  }
  
  @Override
  public void stop() throws Exception {
    super.stop();

    for(Endpoint endpoint : endpoints) {
      endpoint.destroy();
    }
  }
}
