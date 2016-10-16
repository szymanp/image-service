package com.metapx.server.data_model.resource.infrastructure;

public class ETag {
  private final int versionNumber;
  
  public ETag(int versionNumber) {
    this.versionNumber = versionNumber; 
  }
  
  public String getValue() {
    return "W/\"" + versionNumber + "\"";
  }
}
