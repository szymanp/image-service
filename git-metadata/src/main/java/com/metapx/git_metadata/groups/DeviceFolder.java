package com.metapx.git_metadata.groups;

public class DeviceFolder extends Group {
  public DeviceFolder(Api api, GroupTreeRecord treeRecord) {
    super(api, treeRecord);
  }
  public DeviceFolder(Api api, String id, String name) {
    super(api, id, name);
  }

  public String getType() { return DeviceFolderProvider.TYPE; }
}
