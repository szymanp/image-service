package com.metapx.local_repo_server;

import io.vertx.rxjava.core.Vertx;

public class ServerApp {
  public static void main(String[] args) {
    final Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(ServerVerticle.class.getName());
  }
}
