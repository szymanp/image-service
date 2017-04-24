package com.metapx.git_metadata.core.collections;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * A collection that is empty and does not permit any elements.
 */
public class EmptyCollection<T> implements Collection<T> {
  public static <T> EmptyCollection<T> create() {
    return new EmptyCollection<T>();
  }

  public void append(T element) {
    throw new RuntimeException("This collection cannot be modified.");
  }
  public void update(T element) {
    throw new RuntimeException("This collection cannot be modified.");
  }
  public void remove(T element) {
    throw new RuntimeException("This collection cannot be modified.");
  }
  public boolean contains(T element) {
    return false;
  }
  public List<T> list() {
    return Collections.emptyList();
  }
  public Stream<T> stream() {
    return Stream.empty();
  }
}
