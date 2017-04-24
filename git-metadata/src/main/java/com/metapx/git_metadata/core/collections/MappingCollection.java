package com.metapx.git_metadata.core.collections;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A collection that maps elements from the inner collection to a different type.
 */
public class MappingCollection<F, T> implements Collection<T> {
  protected final Collection<F> inner;
  private final MapFunction<T, F> forward;
  private final MapFunction<F, T> reverse;

  public MappingCollection(Collection<F> inner, MapFunction<F, T> reverse, MapFunction<T, F> forward) {
    this.inner = inner;
    this.forward = forward;
    this.reverse = reverse;
  }

  public Collection<F> getInnerCollection() {
    return inner;
  }

  public void append(T element) {
    inner.append(forward.get(element));
  }
  public void update(T element) {
    inner.update(forward.get(element));
  }
  public void remove(T element) {
    inner.remove(forward.get(element));
  }
  public boolean contains(T element) {
    return inner.contains(forward.get(element));
  }
  public List<T> list() {
    return stream().collect(Collectors.toList());
  }
  public Stream<T> stream() {
    return inner.stream().map(element -> reverse.get(element));
  }

  public interface MapFunction<F, T> {
    T get(F value);
  }
}
