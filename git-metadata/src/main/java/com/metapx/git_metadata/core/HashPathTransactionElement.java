package com.metapx.git_metadata.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.metapx.git_metadata.core.HashPath.Target;

public class HashPathTransactionElement<T extends TransactionElement> implements TransactionElement {
  private final HashPath hashPath;
  private final Map<String, Entry<T>> existing = new HashMap<String, Entry<T>>();
  private final TransactionElementBuilder<T> builder;

  public HashPathTransactionElement(HashPath hashPath, TransactionElementBuilder<T> builder) {
    this.hashPath = hashPath;
    this.builder = builder;
  }

  public T get(String hash) {
    if (existing.containsKey(hash)) {
      return existing.get(hash).transactionElement;
    } else {
      final Target target = hashPath.getTarget(hash);
      final T transactionElement = builder.createTransactionElement(target);
      existing.put(hash, new Entry<T>(transactionElement, target));
      return transactionElement;
    }
  }

  public Optional<T> getIfExists(String hash) {
    if (existing.containsKey(hash)) {
      return Optional.of(existing.get(hash).transactionElement);
    } else {
      final Optional<Target> target = hashPath.getTargetIfExists(hash);
      if (target.isPresent()) {
        final T transactionElement = builder.createTransactionElement(target.get());
        existing.put(hash, new Entry<T>(transactionElement, target.get()));
        return Optional.of(transactionElement);
      }
    }
    return Optional.empty();
  }

  /**
   * Returns a stream with all available hashes.
   * 
   * Note: Currently the method returns only committed elements.
   */
  public Stream<String> allHashes() {
    try {
      return hashPath.getAllTargets().map(target -> target.getHash());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a stream with all available transaction elements.
   * 
   * Note that 
   */
  public Stream<Detached<T>> all() {
    try {
      return Stream.concat(
        existing.entrySet().stream()
          .map(entry -> new Detached<T>(entry.getValue().transactionElement, entry.getKey(), () -> {})),
        hashPath.getAllTargets()
          .filter(target -> !existing.containsKey(target.getHash()))
          .map(target -> {
            final T transactionElement = builder.createTransactionElement(target);
            return new Detached<T>(transactionElement, target.getHash(), () -> {
              if (existing.containsKey(target.getHash())) {
                throw new RuntimeException("Key already exists");
              }
              existing.put(target.getHash(), new Entry<T>(transactionElement, target));
            });
          })
        );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void commit() throws Exception {
    existing.values().forEach(entry -> {
      entry.target.prepare();
      try {
        entry.transactionElement.commit();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  public void rollback() {
    existing.values().forEach(entry -> entry.transactionElement.rollback());
  }

  public interface TransactionElementBuilder<T> {
    T createTransactionElement(Target target);
  }

  private static class Entry<T extends TransactionElement> {
    final T transactionElement;
    final Target target;

    Entry(T transactionElement, Target target) {
      this.transactionElement = transactionElement;
      this.target = target;
    }
  }

  public static class Detached<T extends TransactionElement> {
    final T transactionElement;
    final String hash;
    final Runnable attachFn;

    Detached(T transactionElement, String hash, Runnable attachFn) {
      this.transactionElement = transactionElement;
      this.hash = hash;
      this.attachFn = attachFn;
    }

    public T get() {
      return transactionElement;
    }

    public String getHash() {
      return hash;
    }

    public void attach() {
      attachFn.run();
    }
  }
}
