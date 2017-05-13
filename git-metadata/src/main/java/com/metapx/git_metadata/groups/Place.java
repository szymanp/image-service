package com.metapx.git_metadata.groups;

public class Place extends Group {
  public Place(Api api, GroupTreeRecord treeRecord) {
    super(api, treeRecord);
  }
  public Place(Api api, String id, String name) {
    super(api, id, name);
  }

  public String getType() { return PlaceProvider.TYPE; }
}
