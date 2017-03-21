package com.metapx.git_metadata.references;

public class Reference<T> {
  private final Class<?> clazz;
  private final T id;

  public Reference(Class<?> clazz, T id) {
    this.clazz = clazz;
    this.id = id;
  }

  public Class<?> getObjectClass() { return clazz; }
  public T getObjectId() { return id; }

  public static <T> Reference<T> create(Class<?> clazz, T id) {
    return new Reference<T>(clazz, id);
  }
}
