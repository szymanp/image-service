package com.metapx.git_metadata.groups;

import com.metapx.git_metadata.references.Reference;

public class GroupReference extends Reference<String> {
  public GroupReference(String hash) {
    super(Group.class, hash);
  }
}
