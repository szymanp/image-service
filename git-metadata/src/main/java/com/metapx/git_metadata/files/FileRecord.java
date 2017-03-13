package com.metapx.git_metadata.files;

import com.metapx.git_metadata.core.Record;

public class FileRecord implements Record {
  private String hash;
  private String filetype;
  private int size;
  private int width;
  private int height;
  private String defaultFilename;

  public String getHash() { return hash; }
  public String getFiletype() { return filetype; }
  public int getSize() { return size; }
  public int getWidth() { return width; }
  public int getHeight() { return height; }
  public String getDefaultFilename() { return defaultFilename; }

  public void setHash(String hash) { this.hash = hash; }
  public void setFiletype(String filetype) { this.filetype = filetype; }
  public void setSize(int size) { this.size = size; }
  public void setWidth(int width) { this.width = width; }
  public void setHeight(int height) { this.height = height; }
  public void setDefaultFilename(String filename) { this.defaultFilename = filename; }

  public String[] toArray() {
    return new String[] {
      Integer.toString(size),
      filetype,
      Integer.toString(width),
      Integer.toString(height),
      defaultFilename
    };
  }

  public static FileRecord fromArray(String[] record) {
    if (record.length < 5) throw new RuntimeException("Record is too short");

    final FileRecord result = new FileRecord();
    result.size     = Integer.parseInt(record[0]);
    result.filetype = record[1];
    result.width    = Integer.parseInt(record[2]);
    result.height   = Integer.parseInt(record[3]);
    result.defaultFilename = record[4];
    return result;
  }
}
