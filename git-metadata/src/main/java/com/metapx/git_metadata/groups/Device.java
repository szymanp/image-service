package com.metapx.git_metadata.groups;

public class Device extends Group {
  public Device(Api api, GroupTreeRecord treeRecord) {
    super(api, treeRecord);
  }
  public Device(Api api, String id, String name) {
    super(api, id, name);
  }

  public String getType() { return DeviceProvider.TYPE; }
}
