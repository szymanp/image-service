package com.metapx.local_picture_repo.picture_repo;

import java.io.File;

public interface FileInformation {
  /**
   * @return the file that is described by this object
   */
  public File getFile();
  
  /**
   * @return the digest of this file.
   */
  public String getHash();

  /**
   * @return `true` if this file is an image; otherwise, `false`.
   */
  public boolean isImage();
  
  /**
   * @return the type of this image.
   */
  public String getImageType();

  /**
   * @return the width of the image in pixels.
   */
  public int getWidth();

  /**
   * @return the height of the image in pixels.
   */
  public int getHeight();
}
