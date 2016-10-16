package com.metapx.server.data_model.resource.infrastructure;

/**
 * Provides all the properties needed to identify a resource.
 */
public class ResourceIdentifier {
  private final Class<?> clazz;
  private final Key<?> key;
  
  public ResourceIdentifier(Class<?> clazz, Key<?> key) {
    this.clazz = clazz;
    this.key = key;
  }

  public Class<?> getResourceClass() {
    return clazz;
  }

  public Key<?> getKey() {
    return key;
  }
}
