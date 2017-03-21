package com.metapx.git_metadata.references;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ReferenceService {
  private final Map<Entry, List<Handler>> handlers;

  public ReferenceService() {
    handlers = new HashMap<Entry, List<Handler>>();
  }

  public void register(Class<?> clazz, Operation op, Handler handler) {
    final Entry e = new Entry(clazz, op);

    if (!handlers.containsKey(e)) {
      handlers.put(e, new ArrayList<Handler>());
    }
    handlers.get(e).add(handler);
  }

  public void emit(Message message) throws OperationException {
    final Entry e = new Entry(message.origin.getObjectClass(), message.operation);

    final List<Handler> handlers = this.handlers.get(e);
    if (handlers != null) {
      for(Handler h : handlers) h.accept(message);
    }
  }

  public static MessageBuilder newMessageBuilder(Reference<?> origin, Operation op) {
    return new MessageBuilder(origin, op);
  }

  private class Entry {
    Class<?> clazz;
    Operation operation;

    Entry(Class<?> c, Operation o) {
      clazz = c;
      operation = o;
    }

    @Override
    public boolean equals(Object other) {
      if (other != null && other instanceof Entry) {
        final Entry o = (Entry) other;
        return o.clazz == clazz && o.operation == operation;
      }
      return false;
    }

    @Override
    public int hashCode() {
      return clazz.hashCode() * 31 + operation.hashCode() * 13;
    }
  }

  public enum Operation {
    REFERENCE, UNREFERENCE
  }

  public interface Handler {
    void accept(Message message) throws OperationException;
  }

  public static class Message {
    private final Reference<?> origin;
    private final Operation operation;
    private final List<Reference<?>> refs;

    public Message(Reference<?> origin, Operation op) {
      this.operation = op;
      this.origin = origin;
      this.refs = null;
    }

    Message(Reference<?> origin, Operation op, List<Reference<?>> refs) {
      this.operation = op;
      this.origin = origin;
      this.refs = refs;
    }

    public Reference<?> getOrigin() { return origin; }
    public Operation getOperation() { return operation; }

    public <T extends Reference<?>> Stream<T> getReferences(Class<T> clazz) {
      if (refs != null) {
        return refs.stream().filter(ref -> ref.getClass() == clazz).map(ref -> clazz.cast(ref));
      } else {
        return Stream.empty();
      }
    }
  }

  public static class MessageBuilder {
    private final Reference<?> origin;
    private final Operation operation;
    private final List<Reference<?>> refs;

    MessageBuilder(Reference<?> origin, Operation op) {
      this.origin = origin;
      this.operation = op;
      this.refs = new ArrayList<Reference<?>>();
    }

    MessageBuilder references(Reference<?> ref) {
      refs.add(ref);
      return this;
    }

    Message build() {
      return new Message(origin, operation, refs);
    }
  }

  public static class OperationException extends Exception {
    private static final long serialVersionUID = 1;

    public OperationException(Throwable e) {
      super(e);
    }

    public OperationException(String message) {
      super(message);
    }
  }
}
