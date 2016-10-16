package com.metapx.server.auth;

import com.metapx.server.data_model.jooq.Tables;
import com.metapx.server.data_model.jooq.tables.records.UsersRecord;
import com.metapx.server.data_model.resource.PasswordManager;
import com.metapx.server.util.ClusterWideMap;
import com.metapx.server.util.DataContext;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import rx.Single;
import rx.subjects.AsyncSubject;

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
    ctx.getDslContext()
    .map(dsl -> dsl.selectFrom(Tables.USERS)
        .where(Tables.USERS.HANDLE.equal(username))
        .fetchOne())
    .doOnSuccess(user -> {
      if (user == null) throw new RuntimeException("User does not exist");
    })
    .doOnSuccess(user -> {
      if (!passwordManager.computeHash(password, user.getSalt()).equals(user.getPassword())) {
        throw new RuntimeException("Invalid password");
      }
    }).subscribe(
      (user) -> 
        ClusterWideMap.<String, JsonObject>get(ctx.vertx(), SESSION_MAP)
        .flatMap(map -> createSession(user, map))
        .subscribe(
          (session) -> result.handle(Future.succeededFuture(new AuthenticatedUser(session))),
          (error) -> result.handle(Future.failedFuture(error))
        ),
      (error) -> result.handle(Future.failedFuture(error))
    );
  }
  
  public void authenticate(String token, Handler<AsyncResult<User>> result) {
    ClusterWideMap.<String, JsonObject>get(ctx.vertx(), SESSION_MAP)
    .subscribe(
      (map) -> map.get(token, resSession -> {
        if (resSession.succeeded() && resSession.result() != null) {
          result.handle(Future.succeededFuture(new AuthenticatedUser(new Session(resSession.result()))));
        } else {
          result.handle(Future.failedFuture("Session does not exist"));
        }
      }),
      (error) -> result.handle(Future.failedFuture(error))
    );
  }
  
  public void terminate(Session session, Handler<AsyncResult<Void>> result) {
    ClusterWideMap.<String, JsonObject>get(ctx.vertx(), SESSION_MAP)
    .subscribe(
      (map) -> map.remove(session.getKey(), resSession -> {
        if (resSession.succeeded()) {
          result.handle(Future.succeededFuture());
        } else {
          result.handle(Future.failedFuture(resSession.cause()));
        }
      }),
      (error) -> result.handle(Future.failedFuture(error))
    );
  }
  
  private Single<Session> createSession(UsersRecord user, AsyncMap<String, JsonObject> map) {
    final AsyncSubject<Session> result = AsyncSubject.create();
    Session session = Session.create(user);
    
    map.putIfAbsent(session.getKey(), session.toJson(), (existing) -> {
      if (existing.failed()) {
        result.onError(existing.cause());
      } else if (existing.result() == null) {
        result.onNext(session);
        result.onCompleted();
      } else {
        // TODO: Here we could retry with a different session key. 
        result.onError(new Exception("Session key is already in use"));
      }
    });
    
    return result.toSingle();
  }
}
