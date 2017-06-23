package com.metapx.local_repo_server;

import com.hazelcast.config.Config;
import com.metapx.local_picture_repo.database.ConnectionFactory;
import com.metapx.local_repo_server.picture_repo.PictureRepositoryVerticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.rxjava.core.Vertx;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class ServerApp {
  public static void main(String[] args) {
    final Config hazelcastConfig = new Config();
    hazelcastConfig.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(true);
    
    ConnectionFactory.SharedConnectionPool.create("C:/Users/Szyman/.metapx/metapx");

    ClusterManager mgr = new HazelcastClusterManager(hazelcastConfig);
    VertxOptions options = new VertxOptions().setClusterManager(mgr);
    Vertx.clusteredVertx(options, res -> {
      if (res.succeeded()) {
        Vertx vertx = res.result();
        
        final JsonObject config = new JsonObject();
        config.put("path.scaled-cache", "C:/Users/Szyman/.metapx/scaled-cache");
        
        vertx.deployVerticle(PictureRepositoryVerticle.class.getName(), new DeploymentOptions().setConfig(config));
      }
    });
  }
}
