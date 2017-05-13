package com.metapx.git_metadata.groups;

class PersonProvider implements GroupProvider<Person> {
  final public static String TYPE = "person";
  final private Group.Api api;

  PersonProvider(Group.Api api) {
    this.api = api;
  }

  @Override
  public String getName() {
    return TYPE;
  }
  @Override
  public Person readInstance(GroupTreeRecord treeRecord) {
    return new Person(api, treeRecord);
  }
  @Override
  public Person newInstance(String id, String name) {
    return new Person(api, id, name);
  }
  @Override
  public boolean matches(GroupTreeRecord treeRecord) {
    return TYPE.equals(treeRecord.getType());
  }
  @Override
  public boolean matches(Group group) {
    return group instanceof Person;
  }

  @Override
  public void save(Person group) {
    // TODO
  }
  @Override
  public void delete(Person group) {
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
