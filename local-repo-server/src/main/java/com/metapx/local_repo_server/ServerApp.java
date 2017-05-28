package com.metapx.local_repo_server;

import com.hazelcast.config.Config;

import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.rxjava.core.Vertx;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class ServerApp {
  public static void main(String[] args) {
    final Config hazelcastConfig = new Config();
    hazelcastConfig.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(true);

    ClusterManager mgr = new HazelcastClusterManager(hazelcastConfig);
    VertxOptions options = new VertxOptions().setClusterManager(mgr);
    Vertx.clusteredVertx(options, res -> {
      if (res.succeeded()) {
        Vertx vertx = res.result();
        vertx.deployVerticle(ServerVerticle.class.getName());
      }
    });
  }
}
