package com.metapx.git_metadata.core;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.metapx.git_metadata.core.collections.KeyedCollection;
import com.metapx.git_metadata.files.FileRecord;
import com.metapx.git_metadata.files.FileService;
import com.metapx.git_metadata.groups.Group;
import com.metapx.git_metadata.groups.GroupCollection;
import com.metapx.git_metadata.groups.GroupService;
import com.metapx.git_metadata.pictures.Picture;
import com.metapx.git_metadata.pictures.PictureService;
import com.metapx.git_metadata.references.ReferenceService;

public class MetadataRepository {
  private final File root;
  private final TransactionControlImpl transactionControl;
  private final IdService idService;
  private final ReferenceService referenceService;
  private final FileService fileService;
  private final PictureService pictureService;
  private final GroupService groupService;

  public MetadataRepository(File repositoryRoot) throws RepositoryException {
    root = repositoryRoot;

    if (root.exists() && !root.isDirectory()) {
      throw new RepositoryException("The repository root is not a folder");
    }

    transactionControl = new TransactionControlImpl();
    referenceService = new ReferenceService();
    idService = new IdService(new File(root, "ids"), transactionControl);
    fileService = new FileService(getSubdirectory("files"), transactionControl, referenceService);
    pictureService = new PictureService(getSubdirectory("pictures"), transactionControl, idService, referenceService);
    groupService = new GroupService(getSubdirectory("groups"), transactionControl, idService, referenceService);
  }

  /**
   * @return the root directory of this repository
   */
  public File getRoot() {
    return this.root;
  }

  // Inner services
  
  public FileService fileApi() {
    return fileService;
  }

  public IdService identifierApi() {
    return idService;
  }

  public PictureService pictureApi() {
    return pictureService;
  }

  public GroupService groupApi() {
    return groupService;
  }
  
  // Convenience methods
  
  public KeyedCollection<String, FileRecord> files() {
    return fileService.files();
  }
  
  public String createId(String idType) {
    return idService.createId(idType);
  }
  
  public KeyedCollection<String, Picture> pictures() {
    return pictureService.pictures();
  }
  
  public <T extends Group> GroupCollection<T> groups(Class<T> clazz) {
    return groupService.groups(clazz);
  }

  public GroupCollection<Group> groups() {
    return groupService.groups();
  }

  // Transaction control
  
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
