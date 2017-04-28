package com.metapx.git_metadata.references;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class ReferenceService {
  private PublishSubject<Message<?, ?>> emitter = PublishSubject.create();

  public ReferenceService() {}

  public void emit(Message<?, ?> message) {
    emitter.onNext(message);
  }

  public Observable<Message<?, ?>> messages() {
    return emitter;
  }

  @SuppressWarnings("unchecked")
  public <S extends Reference<?>, T extends Reference<?>> Observable<Message<S, T>> messages(Class<S> source, Class<T> target) {
    return emitter
      .filter(message -> source.isInstance(message.source()) && target.isInstance(message.target()))
      .map(message -> (Message<S, T>) message);
  }

  public enum Operation {
    REFERENCE, UNREFERENCE
  }
  public static class Message<S extends Reference<?>, T extends Reference<?>> {
    private final S source;
    private final Operation operation;
    private final T target;

    private Message(S source, Operation operation, T target) {
      this.source = source;
      this.operation = operation;
      this.target = target;
    }

    public static <S extends Reference<?>, T extends Reference<?>> Message<S, T> create(S source, Operation operation, T target) {
      return new Message<S, T>(source, operation, target);
    }

    public S source() { return source; }
    public Operation operation() { return operation; }
    public T target() { return target; }

    @Override
    public String toString() {
      return "Message(" + source.toString() + ", " + operation.toString() + ", " + target.toString() + ")";
    }
  }
}
