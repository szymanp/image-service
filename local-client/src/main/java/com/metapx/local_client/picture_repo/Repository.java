package com.metapx.local_client.picture_repo;

import static com.metapx.local_client.database.jooq.Tables.*;

import java.io.File;
import java.sql.Connection;
import java.util.Arrays;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.metapx.local_client.database.jooq.tables.records.FolderRecord;

public final class Repository {
  private final FolderRecord rootFolder;
  private final DSLContext db;

  public Repository(Connection databaseConnection) {
    db = DSL.using(databaseConnection, SQLDialect.H2);
    rootFolder = new FolderRecord();
  }

  public void addFile(File fileToAdd) {
    FolderRecord folder = getFolder(fileToAdd.getParentFile());

    System.out.println(folder);
  }

  private FolderRecord getFolder(File folder) {
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
}
