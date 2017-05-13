package com.metapx.git_metadata.groups;

import com.metapx.git_metadata.core.TransactionElement;

public interface GroupProvider<T extends Group> extends TransactionElement {
  /**
   * @return A user-friendly name for the class of groups provided by this class. For example: "person".
   */
  String getName();
  T readInstance(GroupTreeRecord treeRecord);
  boolean matches(GroupTreeRecord treeRecord);
  boolean matches(Group group);

  T newInstance(String id, String name);

  /** Saves a new or existing group. */
  void save(T group);
  /** Deletes an existing group. */
  void delete(T group);
}
