package com.metapx.local_picture_repo;

/**
 * Provides state information for another object
 */
public class ObjectWithState<T> {
  public enum State { NEW, EXISTING, DELETED };

  final private State state;
  final private T object;

  public static <T> ObjectWithState<T> newObject(T object) {
    return new ObjectWithState<T>(object, State.NEW);
  }

  public static <T> ObjectWithState<T> existingObject(T object) {
    return new ObjectWithState<T>(object, State.EXISTING);
  }

  public static <T> ObjectWithState<T> deletedObject(T object) {
    return new ObjectWithState<T>(object, State.DELETED);
  }

  public ObjectWithState(T object, State state) {
    this.object = object;
    this.state = state;
  }

  public State state() { return state; }
  public T get() { return object; }
}
