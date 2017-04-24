package com.metapx.git_metadata.core;

public class OneStringRecord implements Record {
  private String value;

  public OneStringRecord() {
    value = "";
  }

  public OneStringRecord(String value) {
    this.value = value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public String[] toArray() { return new String[] { value }; }
  public static OneStringRecord fromArray(String[] fields) { return new OneStringRecord(fields[0]); }
}
