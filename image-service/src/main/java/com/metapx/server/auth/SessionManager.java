package com.metapx.server.auth;

import com.metapx.server.data_model.jooq.Tables;
import com.metapx.server.data_model.jooq.tables.records.UsersRecord;
import com.metapx.server.util.DataContext;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

public class SessionManager implements AuthProvider {
  private static final PasswordManager passwordManager = new PasswordManager.Default();
  private DataContext ctx;
  
  public SessionManager(DataContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public void authenticate(JsonObject credentials, Handler<AsyncResult<User>> result) {
    if (credentials.containsKey("username") && credentials.containsKey("password")) {
      this.authenticate(credentials.getString("username"), credentials.getString("password"), result);
    } else {
      result.handle(Future.failedFuture("Unsupported credentials format"));
    }
  }
  
  public void authenticate(String username, String password, Handler<AsyncResult<User>> result) {
    ctx.<User>executeBlocking((dsl, future) -> {
      UsersRecord user = dsl.selectFrom(Tables.USERS)
          .where(Tables.USERS.HANDLE.equal(username))
          .fetchOne();

      System.out.println(username);
      System.out.println(user);
    
      if (user == null) {
        future.fail("User does not exist");
        return;
      }
      
      if (passwordManager.computeHash(password, user.getSalt()).equals(user.getPassword())) {
        future.complete(new AuthenticatedUser(user.getId(), user.getHandle(), user.getDisplayName()));
      } else {
        future.fail("Invalid password");
      }
    }, result);
  }
}
