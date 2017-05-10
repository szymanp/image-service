package com.metapx.local_client.cli;

public class GroupPath {
  /**
   * Creates a new GroupPath from a path string.
   * @param path  A path string where the element separator is "/".
   */
  public static GroupPath split(String path) {
    if (path.startsWith("/")) path = path.substring(1);
    if (path.endsWith("/")) path = path.substring(0, path.length() - 1);

    return new GroupPath(path.split("/"));
  }
  
  private final String[] parts; 
  
  public GroupPath(String[] parts) {
    this.parts = parts;
  }

  public String[] getParts() {
    return parts.clone();
  }
  
  /**
   * @return true if this path denotes the root of the group hierarchy, otherwise, false.
   */
  public boolean isRootPath() {
    return parts.length == 1 && parts[0].equals(""); 
  }

  /**
   * @return the path represented by this object as a string.
   */
  public String getPath() {
    return String.join("/", parts);
  }
}
