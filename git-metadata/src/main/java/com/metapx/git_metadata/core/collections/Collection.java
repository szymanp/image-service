package com.metapx.git_metadata.core.collections;

import java.util.List;
import java.util.stream.Stream;

public interface Collection<T> {
  /**
   * Appends a new element to this collection.
   * If the collection already contains this element, an exception is thrown.
   */
  void append(T element);
  /**
   * Updates an existing element in this collection.
   * If the collection does not contain this element, an exception is thrown.
   */
  void update(T element);
  /**
   * Removes an existing element from this collection.
   * If the collection does not contain this element, then this method does nothing.
   */
  void remove(T element);
  boolean contains(T element);
  List<T> list();
  Stream<T> stream();
}
