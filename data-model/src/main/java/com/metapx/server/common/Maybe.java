package com.metapx.server.common;

public class Maybe<T> {
  final boolean hasValue;
  final T value;

  private Maybe(boolean hasValue, T value) {
    this.hasValue = hasValue;
    this.value = value;
  }

  public static <T> Maybe<T> of(T value) {
    return new Maybe<T>(true, value);
  }

  public static <T> Maybe<T> empty() {
    return new Maybe<T>(false, null);
  }

  public boolean exists() {
    return hasValue;
  }

  public T get() {
    return value;
  }
}
