package com.metapx.server.workflow;

public class UserKey {

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
  
  public int getValue() {
    return key;
  }
}
