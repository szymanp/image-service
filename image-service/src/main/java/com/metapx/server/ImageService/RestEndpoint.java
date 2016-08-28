package com.metapx.server.ImageService;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public final class RestEndpoint {

  DataSource dataSource;
  
  public RestEndpoint(DataSource dataSource) {
    this.dataSource = dataSource;
  }
  
  final public void expectApplicationJson(RoutingContext ctx) {
    if (!ctx.request().getHeader("content-type").equalsIgnoreCase("application/json")) {
      throw new HttpException(HttpURLConnection.HTTP_UNSUPPORTED_TYPE);
    }
  }
  
  public  <T> Handler<RoutingContext> wrap(ServiceHandler<T> handler) {
    return (RoutingContext ctx) -> {
      try (Connection connection = this.dataSource.getConnection()) {
        final DSLContext dsl = DSL.using(connection, SQLDialect.POSTGRES);
        
        try {
          T result = handler.handle(ctx, dsl);
          
          if (result != null) {
            ctx.response().putHeader("content-type", "application/json; charset=utf-8");
            ctx.response().end(Json.encodePrettily(result));
          } else {
            ctx.response().setStatusCode(HttpURLConnection.HTTP_NOT_FOUND).end();
          }

        } catch (HttpException e) {
          e.sendResponse(ctx.response());
        }
      } catch (SQLException e) {
        // TODO
        ctx.response().end(e.toString());
      }
    };
  }
    
  @FunctionalInterface
  interface ServiceHandler<T> {
    T handle(RoutingContext ctx, DSLContext dslContext);
  }
}
