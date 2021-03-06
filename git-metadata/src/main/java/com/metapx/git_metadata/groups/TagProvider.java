package com.metapx.git_metadata.groups;

class TagProvider implements GroupProvider<Tag> {
  final public static String TYPE = "tag";
  final private Group.Api api;

  TagProvider(Group.Api api) {
    this.api = api;
  }

  @Override
  public String getName() {
    return TYPE;
  }
  @Override
  public Tag readInstance(GroupTreeRecord treeRecord) {
    return new Tag(api, treeRecord);
  }
  @Override
  public Tag newInstance(String id, String name) {
    return new Tag(api, id, name);
  }
  @Override
  public boolean matches(GroupTreeRecord treeRecord) {
    return TYPE.equals(treeRecord.getType());
  }
  @Override
  public boolean matches(Group group) {
    return group instanceof Tag;
  }

  @Override
  public void save(Tag group) {
    /* do nothing */
  }
  @Override
  public void delete(Tag group) {
    /* do nothing */
  }

  @Override
  public void commit() throws Exception {
    /* do nothing */
  }
  @Override
  public void rollback() {
    /* do nothing */
  }
}
