package com.metapx.server.ImageService;

import static com.metapx.server.data_model.jooq.Tables.*;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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
import io.vertx.ext.web.handler.BodyHandler;

public class UserEndpoint {

  final private RestEndpoint rest;
  
  public UserEndpoint(DataSource dataSource) {
    rest = new RestEndpoint(dataSource);
  }

  public void register(Router router) {
    router.route(HttpMethod.GET, "/users/:id").blockingHandler(rest.wrap(this::getUser));
    router.route(HttpMethod.POST, "/users").handler(BodyHandler.create());
    router.route(HttpMethod.POST, "/users").blockingHandler(rest.wrap(this::createUser));
  }
  
  public User getUser(RoutingContext ctx, DSLContext dsl) {
    final int id;
    try {
      id = Integer.parseInt(ctx.request().getParam("id"));
    } catch (NumberFormatException e) {
      throw new HttpException(HttpURLConnection.HTTP_NOT_FOUND);
    }

    List<User> users = dsl.select()
      .from(USERS)
      .where(USERS.ID.eq(id))
      .fetch(r -> new User((UsersRecord)r));
    
    return users.size() == 1 ? users.get(0) : null;
  }
  
  public User createUser(RoutingContext ctx, DSLContext dsl) {
    rest.expectApplicationJson(ctx);
   
    final User user = Json.decodeValue(ctx.getBodyAsString(), User.class);
    user.save(dsl);
    
    return user;
  }
}
