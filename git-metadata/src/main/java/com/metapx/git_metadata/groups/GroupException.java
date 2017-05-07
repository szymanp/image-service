package com.metapx.git_metadata.groups;

public abstract class GroupException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public GroupException(String message) {
    super(message);
  }

  public static class InvalidNameException extends GroupException {
    private static final long serialVersionUID = 1L;
    public final String name;
    
    public InvalidNameException(String name) {
      super("The name '" + name + "' is not allowed for a group.");
      this.name = name;
    }
  }
  
  public static class AlreadyExistsException extends GroupException {
    private static final long serialVersionUID = 1L;
    public final String name;
    
    public AlreadyExistsException(String name) {
      super("Group '" + name + "' already exists in the same location.");
      this.name = name;
    }
  }
}
