package com.metapx.server.ImageService;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.metapx.server.auth.LogoutHandler;
import com.metapx.server.auth.SessionManager;
import com.metapx.server.auth.SessionTokenHandler;
import com.metapx.server.data_model.resource.UserService;
import com.metapx.server.util.DataContext;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BasicAuthHandler;

public class RestVerticle extends AbstractVerticle {

  final static private ApplicationContext context = new ClassPathXmlApplicationContext("services.xml");

  @Override
  public void start() throws Exception {
    super.start();
    
    DataSource dataSource = context.getBean("dataSource", DataSource.class);
    DataContext dataContext = new DataContext(vertx, dataSource);
    
    System.out.println("Hello World from RestVerticle!");
    
    HttpServer httpServer = vertx.createHttpServer();
    Router router = Router.router(vertx);
    
    SessionManager sessionManager = new SessionManager(dataContext);
    router.route("/auth/*").handler(BasicAuthHandler.create(sessionManager, "image-service"));
    router.route("/auth/*").handler(SessionTokenHandler.create());
    router.route("/auth/logout").handler(new LogoutHandler(sessionManager));
    
    router.route("/hello").handler(routingContext -> {
      routingContext.response().end("Hello world");
    });
    router.route("/auth/*").handler(routingContext -> {
      routingContext.response().end("Authenticated");
    });
    
    new ResourceEndpoint(dataContext, new UserService(), "user").register(router);
    
    httpServer.requestHandler(router::accept).listen(config().getInteger("http.port", 8080));
  }
}
