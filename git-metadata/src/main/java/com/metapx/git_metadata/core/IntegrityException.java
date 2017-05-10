package com.metapx.git_metadata.core;

import com.metapx.git_metadata.references.Reference;

/**
 * An exception indicating a problem with the repository integrity.
 */
public class IntegrityException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public IntegrityException(String message) {
    super(message);
  }

  public IntegrityException(Reference<?> ref, String message) {
    super(
      ref.getObjectClass().getSimpleName()
      + "(" + ref.getObjectId().toString() + "): "
      + message
    );
  }

}
