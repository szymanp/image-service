package com.metapx.git_metadata.groups;

public class Tag extends Group {
  public Tag(Api api, GroupTreeRecord treeRecord) {
    super(api, treeRecord);
  }
  public Tag(Api api, String id, String name) {
    super(api, id, name);
  }

  public String getType() { return TagProvider.TYPE; }
}
