package com.metapx.git_metadata.core.collections;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.metapx.git_metadata.core.Record;
import com.metapx.git_metadata.core.RecordReader;
import com.metapx.git_metadata.core.UpdatableRecordFile;

public class RecordFileCollection<R extends Record> implements KeyedCollection<String, R> {
  private final UpdatableRecordFile<R> recordFile;
  private final KeyReader<R> keyReader;

  public RecordFileCollection(File file, RecordReader<R> reader, KeyReader<R> keyReader) {
    recordFile = new UpdatableRecordFile<R>(file, fields -> reader.read(fields));
    this.keyReader = keyReader;
  }

  public void append(R element) {
    recordFile.append(element);
  }

  public void update(R element) {
    recordFile.update(element);
  }

  public void remove(R element) {
    recordFile.delete(keyReader.keyOf(element));
  }

  public boolean contains(R element) {
    return recordFile.findWithKey(keyReader.keyOf(element)).isPresent();
  }

  public Optional<R> findWithKey(String key) {
    return recordFile.findWithKey(key);
  }

  public List<R> list() {
    return stream().collect(Collectors.toList());
  }

  public Stream<R> stream() {
    return recordFile.all();
  }

  public UpdatableRecordFile<R> getRecordFile() {
    return recordFile;
  }

  public interface KeyReader<R> {
    String keyOf(R record);
  }
}
