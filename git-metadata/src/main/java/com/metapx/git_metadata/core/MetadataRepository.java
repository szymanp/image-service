package com.metapx.git_metadata.core;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.metapx.git_metadata.files.FileService;

public class MetadataRepository {
  private final File root;
  private final TransactionControlImpl transactionControl;
  private final FileService fileService;

  public MetadataRepository(File repositoryRoot) throws RepositoryException {
    root = repositoryRoot;

    if (root.exists() && !root.isDirectory()) {
      throw new RepositoryException("The repository root is not a folder");
    }

    transactionControl = new TransactionControlImpl();
    fileService = new FileService(getSubdirectory("files"), transactionControl);
  }

  /**
   * @returns the root directory of this repository
   */
  public File getRoot() {
    return this.root;
  }

  public FileService files() {
    return fileService;
  }

  public void commit() throws Exception {
    transactionControl.commit();
  }

  public void rollback() {
    transactionControl.rollback();
  }

  private File getSubdirectory(String name) {
    final File subdir = new File(root, name);
    if (!subdir.exists()) {
      subdir.mkdir();
    }
    return subdir;
  }

  public static class RepositoryException extends Exception {
    static final long serialVersionUID = 0;
    RepositoryException(String message) {
      super(message);
    }
  }

  private static class TransactionControlImpl implements TransactionControl {
    private final Set<TransactionElement> transaction = new HashSet<TransactionElement>();

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
  }
}
