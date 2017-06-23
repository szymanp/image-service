package com.metapx.local_picture_repo.scaling;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.metapx.local_picture_repo.ResolvedFile;

public interface ScaledPictureProvider {
  public enum Status { MISSING, IN_PROGRESS, EXISTS };

  /**
   * @return the status of the particular scaled image.
   */
  public Status getScaledImageStatus(ResolvedFile original, Dimension dim);

  /**
   * Returns the scaled image, but only if it already exists.
   * @param original
   * @param dim
   */
  public Optional<File> getScaledImageIfExists(ResolvedFile original, Dimension dim);
  
  /**
   * Returns a scaled image file, creating it if necessary.
   * 
   * If another process or thread is already creating the same scaled file, then this method
   * will put the thread to sleep until the file is created.
   * 
   * @param original
   * @param dim
   * @return the scaled file.
   * @throws IOException
   * @throws InterruptedException
   */
  public File getScaledImage(ResolvedFile original, Dimension dim) throws IOException, InterruptedException;
}
