package com.metapx.git_metadata.core.collections;

import java.util.List;
import java.util.stream.Stream;

/**
 * A collection that delegates to an inner collection.
 */
public class SkeletonCollection<T> implements Collection<T> {
  protected Collection<T> inner;

  public SkeletonCollection() {
    this.inner = EmptyCollection.create();
  }
  public SkeletonCollection(Collection<T> inner) {
    this.inner = inner;
  }

  public void append(T element) {
    inner.append(element);
  }
  public void update(T element) {
    inner.update(element);
  }
  public void remove(T element) {
    inner.remove(element);
  }
  public boolean contains(T element) {
    return inner.contains(element);
  }
  public List<T> list() {
    return inner.list();
  }
  public Stream<T> stream() {
    return inner.stream();
  }
}
