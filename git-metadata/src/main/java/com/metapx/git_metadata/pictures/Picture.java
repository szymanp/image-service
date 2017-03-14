package com.metapx.git_metadata.pictures;

import java.util.ArrayList;
import java.util.List;

import com.metapx.git_metadata.core.Record;

public class Picture {
  public enum FileType { 
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
    private FileType type;

    public FileLine() {}
    public FileLine(String hash, FileType type) {
      this.hash = hash;
      this.type = type;
    }

    public String getFileHash() { return hash; }
    public FileType getFiletype() { return type; }

    public void setFileHash(String hash) { this.hash = hash; }
    public void setFiletype(FileType filetype) { this.type = filetype; }

    public String[] toArray() {
      return new String[] {
        hash,
        type.name().toLowerCase()
      };
    }

    public static FileLine fromArray(String[] record) {
      if (record.length < 2) throw new RuntimeException("Record is too short");

      final FileLine result = new FileLine();
      result.hash = record[0];
      result.type = FileType.valueOf(record[1].toUpperCase());
      return result;
    }
  }
}
