package com.metapx.local_client.commands;

/**
 * Indicates an error with processing a single item that does not affects other items.
 */
public class ItemException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public ItemException(String message) {
    super(message);
  }
  
  public ItemException(Throwable t) {
    super(t);
  }
}
