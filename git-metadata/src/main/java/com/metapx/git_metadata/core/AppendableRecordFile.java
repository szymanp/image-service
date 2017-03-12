package com.metapx.git_metadata.core;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Iterables;

/**
 * A record file that can only be appended to.
 */
public class AppendableRecordFile<T extends Record> extends RecordFile<T> {
  private final List<String[]> transaction = new ArrayList<String[]>();

  public AppendableRecordFile(File recordFile, RecordReader<T> reader) {
    super(recordFile, reader);
  }

  public void append(T record) {
    transaction.add(record.toArray());
  }

  public Optional<T> findWithKey(String key) throws IOException {
    return Stream.concat(
      transaction.stream()
        .filter(fields -> fields[0].equals(key))
        .map(fields -> reader.read(fields)),
      getLines()
        .filter(fields -> fields.iterator().next().equals(key))
        .map(fields -> reader.read(Iterables.toArray(fields, String.class)))
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
}
