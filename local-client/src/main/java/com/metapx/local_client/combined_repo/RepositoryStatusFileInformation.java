package com.metapx.local_client.combined_repo;

import java.util.Optional;

import com.metapx.local_picture_repo.picture_repo.FileInformation;

/**
 * Provides information on a file in conjunction with its status in the repository.
 */
public interface RepositoryStatusFileInformation extends FileInformation {
  /**
   * @return true, if a file with the same hash is registered in the repository; otherwise, false.
   */
  public boolean isKnown();
  
  /**
   * @return true, if this particular file location is tracked in the repository; otherwise, false.
   */
  public boolean isTracked();
  
  /**
   * @return an optional resolving to a tracked file, if this file location is tracked by the repository.   
   */
  public Optional<TrackedFileInformation> getTrackedFile();
  
  /**
   * @return an optional resolving to a file group, if this file has a hash that is registered with the repository.
   */
  public Optional<TrackedFileGroup> getFileGroup();
}
