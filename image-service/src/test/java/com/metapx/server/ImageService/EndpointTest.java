package com.metapx.server.ImageService;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public abstract class EndpointTest {
  
  final static protected ApplicationContext applicationContext = new ClassPathXmlApplicationContext("services.xml");
  protected Vertx vertx;
  protected Integer port;
  protected Connection connection;
  protected DSLContext dsl;

  @Before
  public void setUp(TestContext context) throws IOException, SQLException {
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
    
    // Setup a DSL object and a transaction
    DataSource dataSource = applicationContext.getBean("dataSource", DataSource.class);
    connection = dataSource.getConnection();
    connection.setAutoCommit(false);
    dsl = DSL.using(connection, SQLDialect.POSTGRES);
  }
  
  @After
  public void tearDown(TestContext context) {
    try {
      connection.rollback();
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    vertx.close(context.asyncAssertSuccess());
  }
  
  protected abstract List<String> getVerticles();
}
