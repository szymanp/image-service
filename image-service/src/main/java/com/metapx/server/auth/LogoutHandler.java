package com.metapx.server.auth;

import java.net.HttpURLConnection;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class LogoutHandler implements Handler<RoutingContext> {

  SessionManager sessionManager;
  
  public LogoutHandler(SessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }
  
  @Override
  public void handle(RoutingContext routingContext) {
    final AuthenticatedUser user = (AuthenticatedUser) routingContext.user();
    if (user != null) {
      sessionManager.terminate(user.getSession(), result -> {
        if (result.succeeded()) {
          routingContext.response().setStatusCode(HttpURLConnection.HTTP_NO_CONTENT).end();
        } else {
          routingContext.response().setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR).end();
        }
      });
    } else {
      routingContext.response().setStatusCode(HttpURLConnection.HTTP_UNAUTHORIZED).end();
    }
  }

}
