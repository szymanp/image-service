package com.metapx.git_metadata.core;

public interface RecordReader<T extends Record> {
  T read(Iterable<String> fields);
  T read(String[] fields);
}
