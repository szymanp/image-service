package com.metapx.server.data_model.resource.infrastructure;

public class CrudError extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public static enum ErrorType {
    NOT_FOUND, VALIDATION_FAILURE
  }
  
  public static CrudError notFound(Key<?> key) {
    return new CrudError(ErrorType.NOT_FOUND, "Resource with key \"" + key.toUrlString() + "\" not found");
  }
  
  public static CrudError notFound() {
    return new CrudError(ErrorType.NOT_FOUND, "Resource not found");
  }
  
  public static CrudError validationFailure(Class<?> clazz, String reason) {
    return new CrudError(ErrorType.VALIDATION_FAILURE, reason);
  }
  
  private final ErrorType type;
  
  private CrudError(ErrorType type, String message) {
    super(message);
    this.type = type;
  }
  
  public ErrorType getErrorType() {
    return this.type;
  }
}
