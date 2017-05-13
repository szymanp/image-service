package com.metapx.git_metadata.groups;

class DeviceFolderProvider implements GroupProvider<DeviceFolder> {
  final public static String TYPE = "device-folder";
  final private Group.Api api;

  DeviceFolderProvider(Group.Api api) {
    this.api = api;
  }

  @Override
  public String getName() {
    return TYPE;
  }
  public DeviceFolder readInstance(GroupTreeRecord treeRecord) {
    return new DeviceFolder(api, treeRecord);
  }
  public DeviceFolder newInstance(String id, String name) {
    return new DeviceFolder(api, id, name);
  }
  public boolean matches(GroupTreeRecord treeRecord) {
    return TYPE.equals(treeRecord.getType());
  }
  public boolean matches(Group group) {
    return group instanceof DeviceFolder;
  }

  public void save(DeviceFolder group) {
    /* do nothing */
  }
  public void delete(DeviceFolder group) {
    /* do nothing */
  }

  public void commit() throws Exception {
    /* do nothing */
  }
  public void rollback() {
    /* do nothing */
  }
}
