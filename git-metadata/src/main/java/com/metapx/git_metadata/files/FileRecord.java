package com.metapx.git_metadata.files;

import com.metapx.git_metadata.core.Record;

public class FileRecord implements Record {
  private String hash;
  private String filetype;
  private int size;
  private int width;
  private int height;
  private String defaultFilename;
  private String pictureId = "";

  public String getHash() { return hash; }
  public String getFiletype() { return filetype; }
  public int getSize() { return size; }
  public int getWidth() { return width; }
  public int getHeight() { return height; }
  public String getDefaultFilename() { return defaultFilename; }
  public String getPictureId() { return pictureId; }

  public void setHash(String hash) { this.hash = hash; }
  public void setFiletype(String filetype) { this.filetype = filetype; }
  public void setSize(int size) { this.size = size; }
  public void setWidth(int width) { this.width = width; }
  public void setHeight(int height) { this.height = height; }
  public void setDefaultFilename(String filename) { this.defaultFilename = filename; }
  public void setPictureId(String pictureId) { this.pictureId = pictureId; }

  public String[] toArray() {
    return new String[] {
      Integer.toString(size),
      filetype,
      Integer.toString(width),
      Integer.toString(height),
      defaultFilename,
      pictureId
    };
  }

  public static FileRecord fromArray(String[] record) {
    if (record.length < 6) throw new RuntimeException("Record is too short");

    final FileRecord result = new FileRecord();
    result.size     = Integer.parseInt(record[0]);
    result.filetype = record[1];
    result.width    = Integer.parseInt(record[2]);
    result.height   = Integer.parseInt(record[3]);
    result.defaultFilename = record[4];
    result.pictureId = record[5];
    return result;
  }
}
