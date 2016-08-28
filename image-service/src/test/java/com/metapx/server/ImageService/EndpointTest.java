package com.metapx.server.ImageService;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public abstract class EndpointTest {
  
  protected Vertx vertx;
  protected Integer port;

  @Before
  public void setUp(TestContext context) throws IOException {
    vertx = Vertx.vertx();

    // Let's configure the verticle to listen on the 'test' port (randomly picked).
    // We create deployment options and set the _configuration_ json object:
    ServerSocket socket = new ServerSocket(0);
    port = socket.getLocalPort();
    socket.close();
    
    DeploymentOptions options = new DeploymentOptions()
        .setConfig(new JsonObject().put("http.port", port)
        );

    // We pass the options as the second parameter of the deployVerticle method.
    for(String verticleName : getVerticles()) {
      vertx.deployVerticle(verticleName, options, context.asyncAssertSuccess());
    }
  }
  
  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }
  
  protected abstract List<String> getVerticles();
}
