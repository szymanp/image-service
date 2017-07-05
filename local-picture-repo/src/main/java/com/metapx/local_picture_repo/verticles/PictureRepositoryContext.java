package com.metapx.local_picture_repo.verticles;

import java.sql.Connection;

import org.h2.jdbcx.JdbcConnectionPool;

import com.metapx.local_picture_repo.PictureRepository;
import com.metapx.local_picture_repo.database.ConnectionFactory;
import com.metapx.local_picture_repo.impl.RepositoryImpl;

import io.vertx.rxjava.core.RxHelper;
import io.vertx.rxjava.core.Vertx;
import rx.Single;
import rx.exceptions.Exceptions;

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
    return getPictureRepository(true);
  }
  
  public Single<PictureRepository> getPictureRepository(boolean blocking) {
    final Single<PictureRepository> result =
      Single.<PictureRepository, Connection>using(
      () -> {
        try {
          return pool.getConnection();
        } catch (final Exception e) {
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
          } catch (final Exception e) {
            Exceptions.propagate(e);
          }
        }
      });
    if (blocking) {
      return result
        .subscribeOn(RxHelper.blockingScheduler(vertx));
    } else {
      return result;
    }
  }
}
