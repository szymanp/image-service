package com.metapx.server.util;

import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.LocalMap;
import rx.Single;
import rx.subjects.AsyncSubject;

public class ClusterWideMap {

  /**
   * Returns a cluster-wide map or a instance-wide map, depending on whether the cluster configuration is enabled. 
   * @param vertx
   * @param name
   * @param handler
   */
  public static <K, V> Single<AsyncMap<K, V>> get(Vertx vertx, String name) {
    if (vertx.isClustered()) {
      final AsyncSubject<AsyncMap<K, V>> result = AsyncSubject.create();
      vertx.sharedData().<K, V>getClusterWideMap(name, (res) -> {
        if (res.succeeded()) {
          result.onNext(res.result());
          result.onCompleted();
        } else {
          result.onError(res.cause());
        }
      });
      return result.toSingle();
    } else {
      final LocalMap<K, V> localMap = vertx.sharedData().<K, V>getLocalMap(name);
      return Single.just(new LocalAsyncMap<K, V>(localMap));
    }
  }
}
