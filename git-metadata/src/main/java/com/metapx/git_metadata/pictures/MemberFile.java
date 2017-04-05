package com.metapx.git_metadata.pictures;

import com.metapx.git_metadata.core.Record;
import com.metapx.git_metadata.pictures.Picture.Role;

public class MemberFile implements Record {
  private String hash;
  private Role role;

  public MemberFile() {}

  public MemberFile(String hash, Role role) {
    this.hash = hash;
    this.role = role;
  }

  public String getFileHash() { return hash; }
  public Role getRole() { return role; }

  public void setFileHash(String hash) { this.hash = hash; }
  public void setRole(Role role) { this.role = role; }

  public String[] toArray() {
    return new String[] {
      hash,
      role.name().toLowerCase()
    };
  }

  public static MemberFile fromArray(String[] record) {
    if (record.length < 2) throw new RuntimeException("Record is too short");

    final MemberFile result = new MemberFile();
    result.hash = record[0];
    result.role = Role.valueOf(record[1].toUpperCase());
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (other != null && other instanceof MemberFile) {
      return hash == null ? false : hash.equals(((MemberFile) other).hash);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return hash == null ? 0 : hash.hashCode();
  }
}
