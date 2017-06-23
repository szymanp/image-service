package com.metapx.local_repo_server.picture_repo;

import java.io.File;

import com.metapx.local_repo_server.Endpoint;

import io.vertx.core.http.HttpServerOptions;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.ext.web.Router;

public class PictureRepositoryVerticle extends AbstractVerticle {
  
  private Endpoint[] endpoints;
  
  @Override
  public void start() throws Exception {
    super.start();
    
    final HttpServerOptions options = new HttpServerOptions();
    final HttpServer httpServer = vertx.createHttpServer(options);
    final Router router = Router.router(vertx);
    
    final File scaledCache = new File(config().getString("path.scaled-cache"));
    
    endpoints = new Endpoint[] { new PictureRepositoryEndpoint(vertx, scaledCache) };
    for(Endpoint endpoint : endpoints) {
      endpoint.register(router);
    }
    
    httpServer.requestHandler(router::accept).listen(
      config().getInteger("http.port", 8080),
      config().getString("http.host", "0.0.0.0")
    );
  }
  
  @Override
  public void stop() throws Exception {
    super.stop();

    for(Endpoint endpoint : endpoints) {
      endpoint.destroy();
    }
  }
}
