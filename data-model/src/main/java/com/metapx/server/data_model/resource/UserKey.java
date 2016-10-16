package com.metapx.server.data_model.resource;

public class UserKey implements Key<Integer> {
  
  final private boolean valid;
  final private int key;
  
  public UserKey(String key) {
    int id;
    boolean valid;
    try {
      id = Integer.parseInt(key);
      valid = true;
    } catch (NumberFormatException e) {
      id = 0;
      valid = false;
    }
    this.key = id;
    this.valid = valid;
  }
  
  public UserKey(int key) {
    this.key = key;
    this.valid = true;
  }
  
  public boolean isValid() {
    return valid;
  }
  
  public String toUrlString() {
    return Integer.toString(key);
  }
  
  public Integer getValue() {
    return key;
  }
}
