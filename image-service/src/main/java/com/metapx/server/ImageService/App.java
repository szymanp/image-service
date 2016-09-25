package com.metapx.server.ImageService;

import com.hazelcast.config.Config;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

/**
 * Hello world!
 *
 */
public class App {
  
  public static void main(String[] args) {
    System.out.println("Hello World!");
    //Vertx.vertx().createHttpServer().requestHandler(req -> req.response().end("Hello World!")).listen(8080);
    
    Config hazelcastConfig = new Config();
    hazelcastConfig.getNetworkConfig().getJoin().getTcpIpConfig().addMember("127.0.0.1").setEnabled(true);
    hazelcastConfig.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

    ClusterManager mgr = new HazelcastClusterManager(hazelcastConfig);
    VertxOptions options = new VertxOptions().setClusterManager(mgr);
    Vertx.clusteredVertx(options, res -> {
        if (res.succeeded()) {
            Vertx vertx = res.result();
            vertx.deployVerticle(RestVerticle.class.getName());
        }
    });
  }
}
