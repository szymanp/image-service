package com.metapx.server.auth;

import com.metapx.server.data_model.jooq.Tables;
import com.metapx.server.data_model.jooq.tables.records.UsersRecord;
import com.metapx.server.util.ClusterWideMap;
import com.metapx.server.util.DataContext;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

public class SessionManager implements AuthProvider {
  private static final String SESSION_MAP = SessionManager.class.getCanonicalName(); 
  
  private static final PasswordManager passwordManager = new PasswordManager.Default();
  private DataContext ctx;
  
  public SessionManager(DataContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public void authenticate(JsonObject credentials, Handler<AsyncResult<User>> result) {
    if (credentials.containsKey("username") && credentials.getString("password") != null) {
      this.authenticate(credentials.getString("username"), credentials.getString("password"), result);
    } else if (credentials.containsKey("username")) {
        this.authenticate(credentials.getString("username"), result);
    } else {
      result.handle(Future.failedFuture("Unsupported credentials format"));
    }
  }
  
  public void authenticate(String username, String password, Handler<AsyncResult<User>> result) {
    ctx.<UsersRecord>executeBlocking((dsl, future) -> {
      final UsersRecord user = dsl.selectFrom(Tables.USERS)
          .where(Tables.USERS.HANDLE.equal(username))
          .fetchOne();

      if (user == null) {
        future.fail("User does not exist");
      } else {
        future.complete(user);
      }
    }, (resUser) -> {
      if (!resUser.succeeded()) {
        result.handle(Future.failedFuture(resUser.cause()));
        return;
      }
      final UsersRecord user = resUser.result();
      
      if (passwordManager.computeHash(password, user.getSalt()).equals(user.getPassword())) {
        ClusterWideMap.<String, JsonObject>get(ctx.vertx(), SESSION_MAP, resMap -> {
          if (resMap.succeeded()) {
            createSession(user, resMap.result(), (resSession) -> {
              if (resSession.succeeded()) {
                result.handle(Future.succeededFuture(new AuthenticatedUser(resSession.result())));
              } else {
                result.handle(Future.failedFuture(resSession.cause()));
              }
            });
          } else {
            result.handle(Future.failedFuture(resMap.cause()));
          }
          
        });
      } else {
        result.handle(Future.failedFuture("Invalid password"));
      }
    });
  }
  
  public void authenticate(String token, Handler<AsyncResult<User>> result) {
    ClusterWideMap.<String, JsonObject>get(ctx.vertx(), SESSION_MAP, resMap -> {
      if (resMap.succeeded()) {
        resMap.result().get(token, resSession -> {
          if (resSession.succeeded() && resSession.result() != null) {
            result.handle(Future.succeededFuture(new AuthenticatedUser(new Session(resSession.result()))));
          } else {
            result.handle(Future.failedFuture("Session does not exist"));
          }
        });
      } else {
        result.handle(Future.failedFuture(resMap.cause()));
      }
    });
  }
  
  public void terminate(Session session, Handler<AsyncResult<Void>> result) {
    ClusterWideMap.<String, JsonObject>get(ctx.vertx(), SESSION_MAP, resMap -> {
      if (resMap.succeeded()) {
        resMap.result().remove(session.getKey(), resSession -> {
          if (resSession.succeeded()) {
            result.handle(Future.succeededFuture());
          } else {
            result.handle(Future.failedFuture(resSession.cause()));
          }
        });
      } else {
        result.handle(Future.failedFuture(resMap.cause()));
      }
    });
  }
  
  private void createSession(UsersRecord user, AsyncMap<String, JsonObject> map, Handler<AsyncResult<Session>> result) {
    Session session = Session.create(user);
    
    map.putIfAbsent(session.getKey(), session.toJson(), (existing) -> {
      if (existing.failed()) {
        result.handle(Future.failedFuture(existing.cause()));
      } else if (existing.result() == null) {
        result.handle(Future.succeededFuture(session));
      } else {
        // TODO: Here we could retry with a different session key. 
        result.handle(Future.failedFuture("Session key is already in use"));
      }
    });
  }
}
