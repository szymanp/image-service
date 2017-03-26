package com.metapx.local_client.picture_repo;

import static com.metapx.local_client.database.jooq.Tables.*;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.Arrays;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.metapx.local_client.database.jooq.tables.records.FileRecord;
import com.metapx.local_client.database.jooq.tables.records.FolderRecord;

public final class Repository {
  private final FolderRecord rootFolder;
  private final DSLContext db;

  public Repository(Connection databaseConnection) {
    db = DSL.using(databaseConnection, SQLDialect.H2);
    rootFolder = new FolderRecord();
  }

  public ObjectWithState<FileRecord> addFile(FileInformation fileToAdd) throws RepositoryException, IOException {
    if (!fileToAdd.getFile().exists()) {
      throw new RepositoryException("File does not exist on disk");
    }
    if (!fileToAdd.isImage()) {
      throw new RepositoryException("The specified path does not denote an image file");
    }

    final FolderRecord folder = createFolderForPath(fileToAdd.getFile().getParentFile());

    FileRecord file =
      db.selectFrom(FILE)
      .where(
        FILE.FOLDER_ID.eq(folder.getId())
        .and(FILE.NAME.eq(fileToAdd.getFile().getName()))
      )
      .fetchOne();

    final String hash = fileToAdd.getHash();

    if (file == null) {
      // The file does not exist in the repository yet.
      file = new FileRecord();
      file.attach(db.configuration());
      file.setFolderId(folder.getId());
      file.setName(fileToAdd.getFile().getName());
      file.setSize(new Long(fileToAdd.getFile().length()).intValue());
      file.setHash(hash);
      file.insert();   

      return ObjectWithState.newObject(file);
    } else {
      // The file exists, update the hash if needed.
      if (hash != file.getHash()) {
        // TODO If the hash differes, we might need to clear the reference to the git-metadata file.
        file.setHash(hash);
        file.setSize(new Long(fileToAdd.getFile().length()).intValue());
        file.update();
      }
      return ObjectWithState.existingObject(file);
    }
  }

  /**
   * @return a folder for the given path; if the folder or any intermediate folders do not exist in the repository,
   *         then they are created.
   */
  private FolderRecord createFolderForPath(File folder) {
    final String[] elements = folder.getAbsolutePath().split("\\" + File.separator);

    return Arrays.stream(elements).reduce(rootFolder, this::getFolderOrCreate, (a, b) -> b);
  }

  /**
   * Finds an existing folder or creates a new one.
   */
  private FolderRecord getFolderOrCreate(FolderRecord parentFolder, String name) {
    FolderRecord folder =
      db.selectFrom(FOLDER)
      .where(FOLDER.NAME.eq(name)
             .and(parentFolder.getId() == null ? FOLDER.PARENT_ID.isNull() : FOLDER.PARENT_ID.eq(parentFolder.getId())))
      .fetchOne();

    if (folder == null) {
      folder = new FolderRecord();
      folder.attach(db.configuration());
      folder.setName(name);
      folder.setParentId(parentFolder.getId());
      folder.insert();
    }

    return folder;
  }

  public static class RepositoryException extends Exception {
    static final long serialVersionUID = 0;
    RepositoryException(String message) {
      super(message);
    }
  }
}
