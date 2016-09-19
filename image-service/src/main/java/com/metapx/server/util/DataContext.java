package com.metapx.server.util;

import java.sql.Connection;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import io.vertx.core.Vertx;
import io.vertx.rx.java.RxHelper;
import rx.Single;
import rx.exceptions.Exceptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class DataContext {
  private Vertx vertx;
  private DataSource dataSource;
  
  public DataContext(Vertx vertx, DataSource dataSource) {
    this.vertx = vertx;
    this.dataSource = dataSource;
  }
  
  public Vertx vertx() {
    return this.vertx;
  }
  
  public <T> void executeBlocking(DataContextOperation<T> handler, Handler<AsyncResult<T>> result) {
    vertx.executeBlocking(future -> {
      try (Connection connection = this.dataSource.getConnection()) {
        final DSLContext dsl = DSL.using(connection, SQLDialect.POSTGRES);
        
        future.complete(handler.handle(dsl));
        
      } catch (Exception e) {
        future.fail(e);
      }
    }, result);
  }
  
  public <T> void executeBlocking(DataContextHandler<T> handler, Handler<AsyncResult<T>> result) {
    vertx.executeBlocking(future -> {
      try (Connection connection = this.dataSource.getConnection()) {
        final DSLContext dsl = DSL.using(connection, SQLDialect.POSTGRES);
        
        handler.handle(dsl, future);
        
      } catch (Exception e) {
        future.fail(e);
      }
    }, result);
  }
  
  public Single<DSLContext> getDslContext() {
    return Single.<DSLContext, Connection>using(
        () -> {
          try {
            System.out.println("connection.get");
            return this.dataSource.getConnection();
          } catch (Exception e) {
            throw Exceptions.propagate(e);
          }
        }, 
        (connection) -> {
          if (connection != null) {
            return Single.just(DSL.using(connection, SQLDialect.POSTGRES));
          } else {
            return Single.error(new Exception("Cannot obtain connection"));
          }
        },
        (connection) -> {
          if (connection != null)
            System.out.println("connection.close");
            try {
              connection.close();
            } catch (Exception e) {
              throw Exceptions.propagate(e);
            }
        })
    .subscribeOn(RxHelper.blockingScheduler(vertx));
  }
  
  @FunctionalInterface
  public interface DataContextOperation<T> {
    T handle(DSLContext dslContext);
  }

  @FunctionalInterface
  public interface DataContextHandler<T> {
    void handle(DSLContext dslContext, Future<T> result);
  }
}