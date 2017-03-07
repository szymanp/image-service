package com.metapx.git_metadata.core;

import java.io.File;

/**
 * Splits a hash into fragments and uses them to create a multi-level directory structure.
 */
class HashPath {
  private final File rootDir;
  private final int levels;
  private final int levelLength;

  public HashPath(File rootDir, int levels, int levelLength) {
    this.rootDir = rootDir;
    this.levels = levels;
    this.levelLength = levelLength;
  }

  public HashPath(File rootDir) {
    this(rootDir, 2, 2);
  }

  /**
   * @return the root directory of all paths created by this object
   */
  public File getRootDir() { return this.rootDir; }

  /**
   * Returns a path corresponding to the given hash.
   * 
   * This method returns a `File` object that points to the last remaining hash fragment.
   * All fragments before the last one are created as intermediate directories, and this method
   * makes sure that those directories exists. 
   * 
   * The returned file, on the other hand, may or may not exist. It is up to the caller to decide
   * whether the final fragment will be created as a directory or a regular file.
   */
  public File getTarget(String hash) {
    final String[] fragments = getFragments(hash);
    File result = rootDir;
    for(int i=0;i<fragments.length;i++) {
      result = new File(result, fragments[i]);
      if (i < fragments.length-1 && !result.exists()) {
        result.mkdir();
      }
    }
    return result;
  }

  private String[] getFragments(String hash) {
    final String[] fragments = new String[this.levels + 1];
    if (hash.length() < this.levels * this.levelLength + 1) {
      throw new RuntimeException("The hash is too short");
    }

    for(int i = 0; i<this.levels; i++) {
      final int index = i * this.levelLength;
      fragments[i] = hash.substring(index, index + this.levelLength);
    }
    fragments[this.levels] = hash.substring(this.levelLength * this.levels);

    return fragments;
  }
}
