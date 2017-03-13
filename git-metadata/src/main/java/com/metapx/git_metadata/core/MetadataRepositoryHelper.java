package com.metapx.git_metadata.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MetadataRepositoryHelper {
  private final File root;

  public MetadataRepositoryHelper(File root) {
    this.root = root;
  }

  /**
   * @return `true` if the root folder contains a repository; otherwise, `false`.
   */
  public boolean exists() {
    return root.exists() && root.isDirectory() && !isEmpty(root);
  }

  /**
   * Creates a new empty repository.
   */
  public MetadataRepository create() throws MetadataRepository.RepositoryException, IOException {
    if (exists()) {
      throw new MetadataRepository.RepositoryException("The repository already exists");
    } else if (root.exists() && !root.isDirectory()) {
      throw new MetadataRepository.RepositoryException("File already exists: " + root.toString());
    } else if (!root.exists()) {
      if (!root.mkdirs()) {
        throw new MetadataRepository.RepositoryException("Could not create repository root in " + root.toString());
      }
    }

    // TODO Setup a git repo in this folder.

    return new MetadataRepository(root);
  }

  /**
   * @return `true` if the directory doesn't contain any files.
   */
  private static boolean isEmpty(File root) {
    try {
      List<File> result = new ArrayList<File>();
      walkDirectoryRecursively(root, result);
      return result.size() == 0;
    } catch(IOException e) {
      return false;
    }
  }

  private static void walkDirectoryRecursively(File rootDir, List<File> accumulator) throws IOException {
    try(DirectoryStream<Path> stream = Files.newDirectoryStream(rootDir.toPath())) {
      for (Path path : stream) {
        if (path.toFile().isDirectory()) {
          walkDirectoryRecursively(path.toFile(), accumulator);
          accumulator.add(path.toAbsolutePath().toFile());
        } else if (path.toFile().isFile()) {
          final File target = path.toAbsolutePath().toFile();
          accumulator.add(target);
        }
      }
    }
  }
}