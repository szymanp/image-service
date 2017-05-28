package com.metapx.local_repo_server;

import io.vertx.rxjava.core.Vertx;

public class LocalServerApp {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(LocalServerVerticle.class.getName());
  }
}
