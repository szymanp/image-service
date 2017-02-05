package com.metapx.git_metadata.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MetadataRepository implements TransactionControl {
  public final File root;
  private Set<TransactionElement> transaction;

  /**
   * @return `true` if the given directory contains a repository; otherwise, `false`.
   */
  public static boolean exists(File root) {
    return root.exists() && root.isDirectory();
  }

  /**
   * Creates a new empty repository in the specified folder.
   */
  public static MetadataRepository create(File repositoryRoot) throws RepositoryException, IOException {
    if (repositoryRoot.exists() && !isEmpty(repositoryRoot)) {
      throw new RepositoryException("The repository is not empty");
    } else if (!repositoryRoot.exists()) {
      if (!repositoryRoot.mkdirs()) {
        throw new RepositoryException("Could not create repository root");
      }
    }

    // TODO Setup a git repo in this folder.
    return new MetadataRepository(repositoryRoot);
  }

  public MetadataRepository(File repositoryRoot) throws RepositoryException {
    root = repositoryRoot;

    if (root.exists() && !root.isDirectory()) {
      throw new RepositoryException("The repository root is not a folder");
    }

    transaction = new HashSet<TransactionElement>();
  }

  public void addElementToTransaction(TransactionElement tx) {
    transaction.add(tx);
  }

  public void commit() throws RepositoryException {
    try {
      for(TransactionElement tx : transaction) {
        tx.commit();
      }
    } catch (Exception e) {
      throw new RepositoryException("Commit failed, repository might be in an inconsistent state: " + e.getMessage());
    }
  }

  public void rollback() {
    transaction.forEach(tx -> tx.rollback());
  }

  /**
   * @return `true` if the directory doesn't contain any files.
   */
  private static boolean isEmpty(File root) throws IOException {
    List<File> result = new ArrayList<File>();
    walkDirectoryRecursively(root, result);
    return result.size() == 0;
  }

  private static void walkDirectoryRecursively(File rootDir, List<File> accumulator) throws IOException {
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
