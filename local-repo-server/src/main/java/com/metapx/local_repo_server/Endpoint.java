package com.metapx.local_repo_server;

import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.Router;

public abstract class Endpoint {
  final protected Vertx vertx;
  
  public Endpoint(Vertx vertx) {
    this.vertx = vertx;
  }
  
  public abstract void register(Router router);
  
  public abstract void destroy();
}
