package com.metapx.server.data_model.resource.infrastructure;

/**
 * @param <T> representation class
 * @param <K> key class, e.g. Integer
 */
public interface WriterService<T, K> {

  public interface CreateParameters extends RequestParameters {
  }

  public interface UpdateParameters extends RequestParameters {
    ResourceIdentifier getResourceIdentifier();
  }

  public interface DeleteParameters extends RequestParameters {
    ResourceIdentifier getResourceIdentifier();
  }
  
  Resource<T> create(T value, CreateParameters parameters);
  
  Resource<T> update(K key, T value, UpdateParameters parameters);
  
  void delete(K key, DeleteParameters parameters);
}
