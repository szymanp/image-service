package com.metapx.local_client.commands;

import com.github.rvesse.airline.annotations.Command;
import com.metapx.local_client.daemon.DaemonVerticle;

import io.vertx.rxjava.core.Vertx;

@Command(
  name = "daemon",
  description = "Start in daemon mode"
)
public class DaemonCommand implements Runnable {

  @Override
  public void run() {
    final Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(DaemonVerticle.class.getName());
  }
}
