package com.metapx.server.data_model.resource;

/**
 * @param <T> representation class
 * @param <K> key class, e.g. Integer
 */
public interface WriterService<T, K> {

  Resource<T> create(T value);
  
  Resource<T> update(K key, T value);
  
  void delete(K key);
}
