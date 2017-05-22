package com.metapx.local_picture_repo;

import java.io.File;

public interface ResolvedFile {
  /**
   * @return the file being the target of this object.
   */
  public File getFile();
  
  /**
   * @return the folder containing the file.
   */
  public File getContainingFolder();

  /**
   * Tests if the file exists.
   */
  public boolean exists();

  /**
   * Tests if the disk file's hash matches the registered hash in the repository. 
   */
  public boolean isValid();
  
  /**
   * @return the hash of the referenced file.
   */
  public String getHash();  
  
  /**
   * @return the image widht in pixels.
   */
  public int getWidth();
  
  /**
   * @return the image height in pixels.
   */
  public int getHeight();
  
  /**
   * @return the internal file type of the image, such as "JPEG"
   */
  public String getImageType();

  /**
   * @return the repository that this file was obtained from.
   */
  public PictureRepository getRepository();
}
