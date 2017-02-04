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
  private final HashCalculator defaultHashCalculator;
  private final FolderRecord rootFolder;
  private final DSLContext db;

  public Repository(Connection databaseConnection, HashCalculator defaultHashCalculator) {
    this.defaultHashCalculator = defaultHashCalculator;
    db = DSL.using(databaseConnection, SQLDialect.H2);
    rootFolder = new FolderRecord();
  }

  public void addFile(File fileToAdd) throws RepositoryException, IOException {
    if (!fileToAdd.exists()) {
      throw new RepositoryException("File does not exist on disk");
    }
    if (!fileToAdd.isFile()) {
      throw new RepositoryException("The specified path does not denote a regular file");
    }

    final FolderRecord folder = createFolderForPath(fileToAdd.getParentFile());

    FileRecord file =
      db.selectFrom(FILE)
      .where(
        FILE.FOLDER_ID.eq(folder.getId())
        .and(FILE.NAME.eq(fileToAdd.getName()))
      )
      .fetchOne();

    if (file == null) {
      // The file does not exist in the repository yet.
      file = new FileRecord();
      file.attach(db.configuration());
      file.setFolderId(folder.getId());
      file.setName(fileToAdd.getName());
      file.setSize(new Long(fileToAdd.length()).intValue());
      file.setHash(defaultHashCalculator.getAlgorithm() + ":" + defaultHashCalculator.calculateStringDigest(fileToAdd));
      file.insert();      
    } else {

    }

    System.out.println(file);
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
    RepositoryException(String message) {
      super(message);
    }
  }
}
