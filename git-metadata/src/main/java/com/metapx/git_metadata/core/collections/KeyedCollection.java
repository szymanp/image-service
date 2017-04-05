package com.metapx.git_metadata.core.collections;

import java.util.Optional;

public interface KeyedCollection<K, T> extends Collection<T> {
  Optional<T> findWithKey(K key);
}
