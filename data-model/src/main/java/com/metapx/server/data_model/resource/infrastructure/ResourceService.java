package com.metapx.server.data_model.resource.infrastructure;

public interface ResourceService<T, K> {
  Class<T> getRepresentationClass();
  Key<K> createKey(String key);
}
