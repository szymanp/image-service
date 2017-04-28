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

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (other != null && other instanceof Reference) {
      final Reference<?> otherReference = (Reference<?>) other;
      return clazz.equals(otherReference.clazz) && id.equals(otherReference.id);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return 13 + clazz.hashCode() + id.hashCode() * 7;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "(" + id.toString() + ")";
  }

  public static <T> Reference<T> create(Class<?> clazz, T id) {
    return new Reference<T>(clazz, id);
  }
}
