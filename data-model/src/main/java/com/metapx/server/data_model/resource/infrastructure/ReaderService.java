package com.metapx.server.data_model.resource.infrastructure;

/**
 * @param <T> representation class
 * @param <K> key class, e.g. Integer
 */
public interface ReaderService<T, K> extends ResourceService<T, K> {
  
  public interface ReadParameters extends RequestParameters {
    ResourceIdentifier getResourceIdentifier();
  }

  /**
   * Reads the resource at the given key. 
   * @param key
   * @return
   */
  Resource<T> read(K key, ReadParameters parameters);
}
