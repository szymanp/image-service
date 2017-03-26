package com.metapx.git_metadata.pictures;

import java.util.ArrayList;
import java.util.List;

import com.metapx.git_metadata.core.Record;

public class Picture {
  public enum Role { 
    ROOT,
    THUMBNAIL;
  }

  private String hash;
  private List<FileLine> files = new ArrayList<FileLine>();

  public void setHash(String hash) { this.hash = hash; }
  public String getHash() { return hash; }
  public List<FileLine> getFiles() { return files; }

  public static class FileLine implements Record {
    private String hash;
    private Role role;

    public FileLine() {}
    public FileLine(String hash, Role role) {
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

    public static FileLine fromArray(String[] record) {
      if (record.length < 2) throw new RuntimeException("Record is too short");

      final FileLine result = new FileLine();
      result.hash = record[0];
      result.role = Role.valueOf(record[1].toUpperCase());
      return result;
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      } else if (other != null && other instanceof FileLine) {
        return hash == null ? false : hash.equals(((FileLine) other).hash);
      } else {
        return false;
      }
    }

    @Override
    public int hashCode() {
      return hash == null ? 0 : hash.hashCode();
    }
  }
}
