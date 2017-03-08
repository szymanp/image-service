package com.metapx.git_metadata.core;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Splitter;

public class RecordFile<T extends Record> implements TransactionElement {
  private final Path file;
  private final RecordReader<T> reader;
  private final List<String[]> transaction = new ArrayList<String[]>();

  RecordFile(File recordFile, RecordReader<T> reader) {
    file = recordFile.toPath();
    this.reader = reader;
  }

  void append(T record) {
    transaction.add(record.toArray());
  }

  Optional<T> findWithKey(String key) throws IOException {
    return Stream.concat(
      transaction.stream()
        .filter(fields -> fields[0].equals(key))
        .map(fields -> reader.read(fields)),
      getLines()
        .filter(fields -> fields.iterator().next().equals(key))
        .map(fields -> reader.read(fields))
    ).findFirst();
  }

  public void commit() throws IOException {
    List<String> lines = transaction.stream()
      .map(fields -> Arrays.asList(fields))
      .map(fields -> String.join("\t", fields))
      .collect(Collectors.toList());
    Files.write(file, lines, UTF_8, APPEND, CREATE);

    transaction.clear();
  }

  public void rollback() {
    transaction.clear();
  }

  private Stream<Iterable<String>> getLines() throws IOException {
    final Splitter splitter = Splitter.on('\t');
    return Files.lines(file, UTF_8).map(line -> splitter.split(line));
  }
}
