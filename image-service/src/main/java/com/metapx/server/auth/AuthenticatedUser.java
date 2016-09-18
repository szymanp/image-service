package com.metapx.server.auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;

public class AuthenticatedUser extends AbstractUser {
  private Session session;
  
  public AuthenticatedUser(Session session) {
    this.session = session;
  }
  
  public Session getSession() {
    return session;
  }
  
  @Override
  public JsonObject principal() {
    return session.toJson();
  }

  @Override
  public void setAuthProvider(AuthProvider arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected void doIsPermitted(String arg0, Handler<AsyncResult<Boolean>> result) {
    result.handle(Future.succeededFuture(false));
  }

}
