package com.metapx.git_metadata.groups;

class TagProvider implements GroupProvider<Tag> {
  final public static String TYPE = "tag";
  final private Group.Api api;

  TagProvider(Group.Api api) {
    this.api = api;
  }

  public Tag readInstance(GroupTreeRecord treeRecord) {
    return new Tag(api, treeRecord);
  }
  public Tag newInstance(String id, String name) {
    return new Tag(api, id, name);
  }
  public boolean matches(GroupTreeRecord treeRecord) {
    return TYPE.equals(treeRecord.getType());
  }
  public boolean matches(Group group) {
    return group instanceof Tag;
  }

  public void save(Tag group) {
    /* do nothing */
  }
  public void delete(Tag group) {
    /* do nothing */
  }

  public void commit() throws Exception {
    /* do nothing */
  }
  public void rollback() {
    /* do nothing */
  }
}
