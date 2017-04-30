package com.metapx.git_metadata.groups;

class DeviceProvider implements GroupProvider<Device> {
  final public static String TYPE = "device";
  final private Group.Api api;

  DeviceProvider(Group.Api api) {
    this.api = api;
  }

  public Device readInstance(GroupTreeRecord treeRecord) {
    return new Device(api, treeRecord);
  }
  public Device newInstance(String id, String name) {
    return new Device(api, id, name);
  }
  public boolean matches(GroupTreeRecord treeRecord) {
    return TYPE.equals(treeRecord.getType());
  }
  public boolean matches(Group group) {
    return group instanceof Device;
  }

  public void save(Device group) {
    /* do nothing */
  }
  public void delete(Device group) {
    /* do nothing */
  }

  public void commit() throws Exception {
    /* do nothing */
  }
  public void rollback() {
    /* do nothing */
  }
}
