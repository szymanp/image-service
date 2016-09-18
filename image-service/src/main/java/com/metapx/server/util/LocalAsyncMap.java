package com.metapx.server.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.Future;

/**
 * A wrapper around a LocalMap that provides the same interface as an AsyncMap.
 *
 */
public class LocalAsyncMap<K, V> implements AsyncMap<K, V> {
  LocalMap<K, V> map;
  
  public LocalAsyncMap(LocalMap<K, V> map) {
    this.map = map;
  }
  
  @Override
  public void get(K k, Handler<AsyncResult<V>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(map.get(k)));
  }

  @Override
  public void put(K k, V v, Handler<AsyncResult<Void>> completionHandler) {
    // TODO
  }

  @Override
  public void put(K k, V v, long ttl, Handler<AsyncResult<Void>> completionHandler) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void putIfAbsent(K k, V v, Handler<AsyncResult<V>> completionHandler) {
    completionHandler.handle(Future.succeededFuture(map.putIfAbsent(k, v)));
  }

  @Override
  public void putIfAbsent(K k, V v, long ttl, Handler<AsyncResult<V>> completionHandler) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void remove(K k, Handler<AsyncResult<V>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(map.remove(k)));
  }

  @Override
  public void removeIfPresent(K k, V v, Handler<AsyncResult<Boolean>> resultHandler) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void replace(K k, V v, Handler<AsyncResult<V>> resultHandler) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void replaceIfPresent(K k, V oldValue, V newValue, Handler<AsyncResult<Boolean>> resultHandler) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void clear(Handler<AsyncResult<Void>> resultHandler) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void size(Handler<AsyncResult<Integer>> resultHandler) {
    // TODO Auto-generated method stub
    
  }

}
