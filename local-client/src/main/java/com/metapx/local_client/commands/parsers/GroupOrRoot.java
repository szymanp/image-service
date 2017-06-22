package com.metapx.local_client.commands.parsers;

import com.metapx.git_metadata.groups.Group;

public class GroupOrRoot {
  private final static GroupOrRoot singletonRoot = new GroupOrRoot();
  private final Group group;
  
  public static GroupOrRoot group(Group g) {
    return new GroupOrRoot(g);
  }
  
  public static GroupOrRoot root() {
    return singletonRoot;
  }
  
  private GroupOrRoot(Group g) {
    group = g;
  }
  
  private GroupOrRoot() {
    group = null;
  }
  
  public boolean isRoot() {
    return group == null;
  }
  
  public Group get() {
    if (group == null) {
      throw new RuntimeException("This object represents the root of the group tree.");
    }
    return group;
  }
}
