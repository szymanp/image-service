package com.metapx.git_metadata.core;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.base.Splitter;

abstract class RecordFile<T extends Record> implements TransactionElement {
  protected final Path file;
  protected final RecordReader<T> reader;

  RecordFile(File recordFile, RecordReader<T> reader) {
    file = recordFile.toPath();
    this.reader = reader;
  }

  public abstract void append(T record) throws IOException;

  public abstract Optional<T> findWithKey(String key) throws IOException;

  public abstract void commit() throws IOException;

  public abstract void rollback();

  protected Stream<Iterable<String>> getLines() throws IOException {
    final Splitter splitter = Splitter.on('\t');
    return Files.lines(file, UTF_8).map(line -> splitter.split(line));
  }

  public static class RecordNotFound extends Exception {
    static final long serialVersionUID = 0;
    RecordNotFound(String message) {
      super(message);
    }
  }
}
