package com.metapx.local_client.combined_repo;

import com.metapx.git_metadata.files.FileRecord;
import com.metapx.local_client.picture_repo.FileInformation;

public interface TrackedFileInformation extends FileInformation {
  /**
   * @return the file group providing access to all tracked copies of this file.
   */
  public TrackedFileGroup getFileGroup();

  /**
   * @return the description of this file.
   */
  public FileRecord getFileRecord();
  
  /**
   * @return true, if this file exists and is valid (the content matches); otherwise, false.
   */
  public boolean isValid();
  
  /**
   * @return true, if this file exists; otherwise, false.
   */
  public boolean exists();
}
