package com.metapx.git_metadata.core;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class StringRecord implements Record {
  final public List<String> fields;

  public StringRecord() {
    fields = new ArrayList<String>();
  }

  public StringRecord(List<String> fields) {
    this.fields = fields;
  }

  public StringRecord(String[] fields) {
    this.fields = Arrays.asList(fields);
  }

  public String[] toArray() {
    return fields.toArray(new String[0]);
  }

  public static class Reader implements RecordReader<StringRecord> {
    public StringRecord read(String[] fields) {
      return new StringRecord(fields);
    }
  }
}
