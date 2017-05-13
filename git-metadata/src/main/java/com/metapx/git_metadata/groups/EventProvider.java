package com.metapx.git_metadata.groups;

class EventProvider implements GroupProvider<Event> {
  final public static String TYPE = "event";
  final private Group.Api api;

  EventProvider(Group.Api api) {
    this.api = api;
  }

  @Override
  public String getName() {
    return TYPE;
  }
  @Override
  public Event readInstance(GroupTreeRecord treeRecord) {
    return new Event(api, treeRecord);
  }
  @Override
  public Event newInstance(String id, String name) {
    return new Event(api, id, name);
  }
  @Override
  public boolean matches(GroupTreeRecord treeRecord) {
    return TYPE.equals(treeRecord.getType());
  }
  @Override
  public boolean matches(Group group) {
    return group instanceof Event;
  }

  @Override
  public void save(Event group) {
    // TODO
  }
  @Override
  public void delete(Event group) {
    // TODO
  }
  @Override
  public void commit() throws Exception {
    // TODO
  }
  @Override
  public void rollback() {
    // TODO
  }
}
