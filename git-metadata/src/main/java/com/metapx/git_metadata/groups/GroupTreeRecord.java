package com.metapx.git_metadata.groups;

import com.metapx.git_metadata.core.Record;

public class GroupTreeRecord implements Record {
  private static String emptyHash = new String(new char[36]).replace("\0", "0");

  private String parentHash = "";
  private String groupHash;
  private String type;
  private String name;

  public String getParentHash() { return parentHash; }
  public String getGroupHash() { return groupHash; }
  public String getType() { return type; }
  public String getName() { return name; }

  public void setParentHash(String hash) { parentHash = hash; }
  public void setGroupHash(String hash) { groupHash = hash; }
  public void setType(String t) { type = t; }
  public void setName(String n) { name = n; }

  public String[] toArray() {
    return new String[] {
      parentHash.equals("") ? emptyHash : parentHash,
      groupHash,
      type,
      name
    };
  }

  public static GroupTreeRecord fromArray(String[] record) {
    if (record.length < 4) throw new RuntimeException("Record is too short");

    final GroupTreeRecord result = new GroupTreeRecord();
    result.parentHash = record[0].equals(emptyHash) ? "" : record[0];
    result.groupHash = record[1];
    result.type = record[2];
    result.name = record[3];
    return result;
  }

  public static GroupTreeRecord fromGroup(Group group) {
    final GroupTreeRecord result = new GroupTreeRecord();
    result.parentHash = group.getParent().isPresent() ? group.getParent().get().getId() : emptyHash;
    result.groupHash = group.getId();
    result.type = group.getType();
    result.name = group.getName();
    return result;
  }
}
