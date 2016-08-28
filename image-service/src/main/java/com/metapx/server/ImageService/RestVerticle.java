package com.metapx.server.ImageService;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

public class RestVerticle extends AbstractVerticle {

  final private ApplicationContext context = new ClassPathXmlApplicationContext("services.xml");

  @Override
  public void start() throws Exception {
    super.start();
    
    DataSource dataSource = context.getBean("dataSource", DataSource.class);
    
    System.out.println("Hello World from RestVerticle!");
    
    HttpServer httpServer = vertx.createHttpServer();
    Router router = Router.router(vertx);
    
    router.route("/hello").handler(routingContext -> {
      routingContext.response().end("Hello world");
    });
    
    new UserEndpoint(dataSource).register(router);
    
    httpServer.requestHandler(router::accept).listen(config().getInteger("http.port", 8080));
  }
}
