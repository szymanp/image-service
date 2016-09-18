package com.metapx.server.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.Future;

public class ClusterWideMap {

  /**
   * Returns a cluster-wide map or a instance-wide map, depending on whether the cluster configuration is enabled. 
   * @param vertx
   * @param name
   * @param handler
   */
  public static <K, V> void get(Vertx vertx, String name, Handler<AsyncResult<AsyncMap<K, V>>> handler) {
    if (vertx.isClustered()) {
      vertx.sharedData().<K, V>getClusterWideMap(name, handler);
    } else {
      final LocalMap<K, V> localMap = vertx.sharedData().<K, V>getLocalMap(name);
      handler.handle(Future.succeededFuture(new LocalAsyncMap<K, V>(localMap)));
    }
  }
}
