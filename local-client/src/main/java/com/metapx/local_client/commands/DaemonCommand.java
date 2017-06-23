package com.metapx.local_client.commands;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import com.github.rvesse.airline.annotations.Command;
import com.metapx.local_client.cli.Configuration;
import com.metapx.local_client.daemon.DaemonVerticle;
import com.metapx.local_repo_server.picture_repo.PictureRepositoryVerticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;

@Command(
  name = "daemon",
  description = "Start in daemon mode"
)
public class DaemonCommand implements Runnable {

  @Override
  public void run() {
    final int port = getRandomPort();
    final Configuration conf = Configuration.getDefaultConfiguration();
    
    final JsonObject pictureRepoConfig = new JsonObject();
    pictureRepoConfig.put("http.port", port);
    pictureRepoConfig.put("http.host", "127.0.0.1");
    pictureRepoConfig.put("path.scaled-cache", conf.getConfigurationDirectory().getAbsolutePath() + File.separator + "scaled-cache");

    final JsonObject daemonVerticleConfig = new JsonObject();
    daemonVerticleConfig.put("picture-repo.url", "http://127.0.0.1:" + port + "/");
    System.out.println(port);

    final Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(DaemonVerticle.class.getName(), new DeploymentOptions().setConfig(daemonVerticleConfig));
    vertx.deployVerticle(PictureRepositoryVerticle.class.getName(), new DeploymentOptions().setConfig(pictureRepoConfig));
  }
  
  private int getRandomPort() {
    try {
      final ServerSocket socket = new ServerSocket(0);
      final int port = socket.getLocalPort();
      socket.close();
      return port;
    } catch (IOException e) {
      return 8080;
    }
  }
}
