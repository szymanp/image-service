package com.metapx.server.data_model.resource;

public interface Key<T> {
  /** @return true, if the key is in the valid format; otherwise, false */ 
  boolean isValid();
  
  /** @return the key formatted for use in a url */
  String toUrlString();
  
  /** @return the internal value of the key */
  T getValue();
}
