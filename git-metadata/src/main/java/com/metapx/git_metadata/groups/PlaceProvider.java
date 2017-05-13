package com.metapx.git_metadata.groups;

class PlaceProvider implements GroupProvider<Place> {
  final public static String TYPE = "place";
  final private Group.Api api;

  PlaceProvider(Group.Api api) {
    this.api = api;
  }

  @Override
  public String getName() {
    return TYPE;
  }
  @Override
  public Place readInstance(GroupTreeRecord treeRecord) {
    return new Place(api, treeRecord);
  }
  @Override
  public Place newInstance(String id, String name) {
    return new Place(api, id, name);
  }
  @Override
  public boolean matches(GroupTreeRecord treeRecord) {
    return TYPE.equals(treeRecord.getType());
  }
  @Override
  public boolean matches(Group group) {
    return group instanceof Place;
  }

  @Override
  public void save(Place group) {
    // TODO
  }
  @Override
  public void delete(Place group) {
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
