package com.metapx.local_picture_repo.verticles;

import java.sql.Connection;

import org.h2.jdbcx.JdbcConnectionPool;

import com.metapx.local_picture_repo.PictureRepository;
import com.metapx.local_picture_repo.database.ConnectionFactory;
import com.metapx.local_picture_repo.impl.RepositoryImpl;

import rx.Single;
import rx.exceptions.Exceptions;
import io.vertx.rxjava.core.RxHelper;
import io.vertx.rxjava.core.Vertx;

public class PictureRepositoryContext {
  final Vertx vertx;
  final JdbcConnectionPool pool;
  
  public PictureRepositoryContext(Vertx vertx) {
    this.vertx = vertx;
    pool = ConnectionFactory.SharedConnectionPool.getConnectionPool();
  }
  
  public void close() {
    // do nothing
  }
  
  public Single<PictureRepository> getPictureRepository() {
    return Single.<PictureRepository, Connection>using(
      () -> {
        try {
          return pool.getConnection();
        } catch (Exception e) {
          Exceptions.propagate(e);
          return null;
        }
      },
      (connection) -> {
        if (connection != null) {
          return Single.just(new RepositoryImpl(connection));
        } else {
          return Single.error(new Exception("Cannot obtain a DB connection"));
        }
      },
      (connection) -> {
        if (connection != null) {
          try {
            connection.close();
          } catch (Exception e) {
            Exceptions.propagate(e);
          }
        }
      })
      .subscribeOn(RxHelper.blockingScheduler(vertx));
  }
}
