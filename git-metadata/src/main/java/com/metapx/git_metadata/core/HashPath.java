package com.metapx.git_metadata.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Splits a hash into fragments and uses them to create a multi-level directory structure.
 */
public class HashPath {
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
   */
  public Target getTarget(String hash) {
    final String[] fragments = getFragments(hash);
    File result = rootDir;
    List<File> dirs = new ArrayList<File>();
    for(int i=0;i<fragments.length;i++) {
      result = new File(result, fragments[i]);
      if (i < fragments.length-1 && !result.exists()) {
        dirs.add(result);
      }
    }
    return new Target(result, dirs);
  }

  /**
   * Returns a path corresponding to the given hash, but only if the target exists.
   */
  public Optional<File> getTargetIfExists(String hash) {
    final String[] fragments = getFragments(hash);
    File result = rootDir;
    for(int i=0;i<fragments.length;i++) {
      result = new File(result, fragments[i]);
      if (!result.exists()) {
        return Optional.empty();
      }
    }
    return Optional.of(result);
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

  /**
   * An object representing the location corresponding to a hash.
   * 
   * The `getFile()` method returns a `File` that points to the last remaining hash fragment.
   * This file may or may not exist, and it is not created by this class.
   * 
   * All fragments before the last one are created as intermediate directories when the `prepare()` method is called.
   */
  public final static class Target {
    private final File target;
    private final List<File> fragments;

    protected Target(File target, List<File> fragments) {
      this.target = target;
      this.fragments = fragments;
    }

    public File getFile() {
      return target;
    }
    
    public void prepare() {
      fragments.forEach(fragment -> {
        if (!fragment.exists()) {
          fragment.mkdir();
        }
      });
    }
  }
}
