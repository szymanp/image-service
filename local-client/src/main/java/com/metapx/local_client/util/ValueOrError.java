package com.metapx.local_client.util;

import java.util.function.Supplier;

public class ValueOrError<T> {
  private final T value;
  private final Throwable error;

  public static <T> ValueOrError<T> of(T value) {
    return new ValueOrError<T>(value);
  }
  
  public static <T> ValueOrError<T> error(Throwable error) {
    return new ValueOrError<T>(error);
  }
  
  public static <T> ValueOrError<T> resolve(Supplier<T> supplier) {
    try {
      return new ValueOrError<T>(supplier.get());
    } catch (Exception e) {
      return new ValueOrError<T>(e);
    }
  }

  private ValueOrError(T value) {
    this.value = value;
    this.error = null;
  }
  
  private ValueOrError(Throwable error) {
    this.error = error;
    this.value = null;
  }
  
  public boolean hasValue() {
    return this.value != null;
  }
  
  public boolean hasError() {
    return this.error != null;
  }
  
  public T get() {
    if (value == null) throw new RuntimeException("No value present.");
    return value;
  }
  
  public Throwable error() {
    if (error == null) throw new RuntimeException("No error present.");
    return error;
  }
}
