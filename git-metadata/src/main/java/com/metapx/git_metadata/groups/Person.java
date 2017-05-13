package com.metapx.git_metadata.groups;

public class Person extends Group {
  public Person(Api api, GroupTreeRecord treeRecord) {
    super(api, treeRecord);
  }
  public Person(Api api, String id, String name) {
    super(api, id, name);
  }

  public String getType() { return PersonProvider.TYPE; }
}
