package com.metapx.git_metadata.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MetadataRepository {
  public final File root;
  private boolean isOpen = false;

  public MetadataRepository(File repositoryRoot) throws RepositoryException {
    root = repositoryRoot;

    if (root.exists() && !root.isDirectory()) {
      throw new RepositoryException("The repository root is not a folder");
    }
  }

  public void create() throws RepositoryException, IOException {
    if (root.exists() && !isRootEmpty()) {
      throw new RepositoryException("The repository is not empty");
    } else if (!root.exists()) {
      if (!root.mkdirs()) {
        throw new RepositoryException("Could not create repository root");
      }
    }

    // TODO Setup a git repo in this folder.
  }

  public void open() {
  }

  public void close() {
  }

  public boolean isOpen() {
    return this.isOpen;
  }

  /**
   * @return `true` if the repository root doesn't contain any files.
   */
  private boolean isRootEmpty() throws IOException {
    List<File> result = new ArrayList<File>();
    walkDirectoryRecursively(root, result);
    return result.size() == 0;
  }

  private void walkDirectoryRecursively(File rootDir, List<File> accumulator) throws IOException {
    try(DirectoryStream<Path> stream = Files.newDirectoryStream(rootDir.toPath())) {
      for (Path path : stream) {
        if (path.toFile().isDirectory()) {
          walkDirectoryRecursively(path.toFile(), accumulator);
        } else if (path.toFile().isFile()) {
          final File target = path.toAbsolutePath().toFile();
          accumulator.add(target);
        }
      }
    }
  }

  public static class RepositoryException extends Exception {
    static final long serialVersionUID = 0;
    RepositoryException(String message) {
      super(message);
    }
  }
}