package com.metapx.git_metadata.groups;

public class Event extends Group {
  public Event(Api api, GroupTreeRecord treeRecord) {
    super(api, treeRecord);
  }
  public Event(Api api, String id, String name) {
    super(api, id, name);
  }

  public String getType() { return EventProvider.TYPE; }
}
