package com.metapx.local_picture_repo;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

public interface PictureRepository {
  /**
   * Adds a new file to the repository or updates the hash of an existing file.
   * 
   * @param fileToAdd
   * @return The added repository file wrapped in a state.
   * @throws PictureRepositoryException
   */
  public ObjectWithState<ResolvedFile> addFile(FileInformation fileToAdd) throws PictureRepositoryException;
  
  /**
   * Finds a file in the repository corresponding to a disk file.
   */
  public Optional<ResolvedFile> findFile(File file);
  
  /**
   * Finds all files in the repository matching the given hash.
   * 
   * Note that this method might return files that are registered in the repository,
   * but no longer exist in the registered location. 
   */
  public Stream<ResolvedFile> findFiles(String hash);
  
  /**
   * Finds a file corresponding to the given hash.
   * 
   * This method will only return a file if it exists.
   */
  public Optional<ResolvedFile> findExistingFile(String hash);
}
