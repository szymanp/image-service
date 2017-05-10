package com.metapx.local_client.combined_repo;

import java.util.Optional;
import java.util.stream.Stream;

import com.metapx.git_metadata.files.FileRecord;

/**
 * A group of files with the same hash tracked by the repository.
 */
public interface TrackedFileGroup {
  /**
   * @return the hash of the files in this group.
   */
  public String getHash();

  /**
   * @return a stream of all tracked files belonging to this group.
   */
  public Stream<TrackedFileInformation> getFiles();
  
  /**
   * @return the metadata record describing the files in this group.
   */
  public FileRecord getFileRecord();

  /**
   * Tries to find a tracked file among the files in this group that exists and is valid on the local system.
   */
  public Optional<TrackedFileInformation> getValidFile();

  // getPicture()
}
