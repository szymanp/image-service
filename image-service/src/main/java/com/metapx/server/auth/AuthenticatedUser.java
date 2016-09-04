package com.metapx.server.auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;

public class AuthenticatedUser extends AbstractUser {
  private int id;
  private String handle;
  private String displayName;
  private JsonObject principal;
  
  public AuthenticatedUser(int id, String handle, String displayName) {
    this.id = id;
    this.handle = handle;
    this.displayName = displayName;
  }
  
  @Override
  public JsonObject principal() {
    if (principal == null) {
      principal = new JsonObject()
            .put("username", handle)
            .put("displayName", displayName)
            .put("id", id);
    }
    return principal;
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
