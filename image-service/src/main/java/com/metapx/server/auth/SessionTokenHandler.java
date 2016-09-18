package com.metapx.server.auth;

import java.util.Base64;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * Adds a session token header after initial login.
 *
 */
public class SessionTokenHandler implements Handler<RoutingContext>{
  
  public static final String HEADER = "X-SessionToken";
  
  private static SessionTokenHandler INSTANCE = new SessionTokenHandler();
  
  public static SessionTokenHandler create() {
    return INSTANCE;
  }

  @Override
  public void handle(RoutingContext routingContext) {
    final AuthenticatedUser user = (AuthenticatedUser) routingContext.user();
    if (user != null && user.getSession().isSessionNew()) {
      final String token = Base64.getEncoder().encodeToString(user.getSession().getKey().getBytes());
      routingContext.response().headers().add(HEADER, token);
    }
    routingContext.next();
  }
}
