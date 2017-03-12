package com.metapx.git_metadata.core;

public interface RecordReader<T extends Record> {
  T read(String[] fields);
}
