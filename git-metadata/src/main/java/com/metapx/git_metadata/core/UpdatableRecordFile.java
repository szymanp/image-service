package com.metapx.git_metadata.core;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
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

import com.google.common.collect.Iterators;

/**
 * A record file where records can be appended, updated and deleted.
 */
public class UpdatableRecordFile<T extends Record> extends RecordFile<T> {
  private boolean modified = false;
  private List<String[]> lines;

  public UpdatableRecordFile(File recordFile, RecordReader<T> reader) {
    super(recordFile, reader);
  }

  public void append(T record) {
    getLinesOnDemand().add(record.toArray());
    modified = true;
  }

  public void update(T record) {
    final String[] line = record.toArray();
    final Optional<String[]> forUpdate = findWithKeyInternal(line[0]);
    if (forUpdate.isPresent()) {
      System.arraycopy(line, 0, forUpdate.get(), 0, line.length);
      modified = true;
    } else {
      throw new RecordNotFound("Record with key \"" + line[0] + "\" was not found");
    }
  }

  public boolean delete(String key) {
    final Optional<String[]> forDelete = findWithKeyInternal(key);
    if (forDelete.isPresent()) {
      getLinesOnDemand().remove(forDelete.get());
      modified = true;
      return true;
    } else {
      return false;
    }
  }

  public void clear() {
    lines = new ArrayList<String[]>();
    modified = true;
  }

  public Optional<T> findWithKey(String key) {
    return findWithKeyInternal(key)
      .map(fields -> reader.read(fields));
  }

  public void set(int index, T record) {
    final List<String[]> lines = getLinesOnDemand();
    if (index == lines.size()) {
      lines.add(record.toArray());
    } else {
      lines.set(index, record.toArray());
    }
    modified = true;
  }

  public Optional<T> get(int index) {
    final List<String[]> lines = getLinesOnDemand();
    if (lines.size() > index) {
      return Optional.of(reader.read(lines.get(index)));
    } else {
      return Optional.empty();
    }
  }

  public Stream<T> all() {
    return getLinesOnDemand()
      .stream()
      .<T>map(line -> reader.read(line));
  }

  public void commit() throws IOException {
    if (modified) {
      List<String> outputLines = lines.stream()
        .map(fields -> Arrays.asList(fields))
        .map(fields -> String.join("\t", fields))
        .collect(Collectors.toList());
      Files.write(file, outputLines, UTF_8, TRUNCATE_EXISTING, CREATE);

      modified = false;
    }
  }

  public void rollback() {
    modified = false;
    lines = null;
  }

  private Optional<String[]> findWithKeyInternal(String key) {
    return getLinesOnDemand().stream()
      .filter(fields -> fields[0].equals(key))
      .findFirst();
  }

  private List<String[]> getLinesOnDemand() {
    if (lines == null) {
      lines = getLines()
        .map(fields -> Iterators.toArray(fields.iterator(), String.class))
        .collect(Collectors.toList());
    }
    return lines;
  }
}
