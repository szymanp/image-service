package com.metapx.server.ImageService;

import static com.metapx.server.data_model.jooq.Tables.*;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.metapx.server.data_model.domain.User;
import com.metapx.server.data_model.jooq.tables.records.UsersRecord;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class UserEndpoint {

  DataSource dataSource;
  
  public UserEndpoint(DataSource dataSource) {
    this.dataSource = dataSource;
  }
  
  public void register(Router router) {
    router.route(HttpMethod.GET, "/users/:id").blockingHandler(this::getUser);
  }
  
  public void getUser(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.request().getParam("id"));

    try (Connection connection = this.dataSource.getConnection()) {
      DSLContext dsl = DSL.using(connection, SQLDialect.POSTGRES);
      
      User user = dsl.select()
        .from(USERS)
        .where(USERS.ID.eq(id))
        .fetchOne()
        .map(r -> new User((UsersRecord)r));
      
      ctx.response().putHeader("content-type", "application/json");
      ctx.response().end(Json.encodePrettily(user));
    } catch (SQLException e) {
      ctx.response().end(e.toString());
    }
  }
}
