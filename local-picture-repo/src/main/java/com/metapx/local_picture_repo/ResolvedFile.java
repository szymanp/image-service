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
   * @return the hash of the referenced file.
   */
  public String getHash();  

  /**
   * @return the repository that this file was obtained from.
   */
  public PictureRepository getRepository();
}
