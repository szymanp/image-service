package com.metapx.local_picture_repo.impl;

import static com.metapx.local_picture_repo.database.jooq.Tables.*;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.metapx.local_picture_repo.FileInformation;
import com.metapx.local_picture_repo.ObjectWithState;
import com.metapx.local_picture_repo.PictureRepository;
import com.metapx.local_picture_repo.PictureRepositoryException;
import com.metapx.local_picture_repo.ResolvedFile;
import com.metapx.local_picture_repo.database.jooq.tables.records.FileRecord;
import com.metapx.local_picture_repo.database.jooq.tables.records.FolderRecord;

public final class RepositoryImpl implements PictureRepository {
  private final FolderRecord rootFolder;
  private final DSLContext db;
  private final Map<File, FolderRecord> folders = new HashMap<File, FolderRecord>();
  /** A mapping between a FolderRecord ID and a file path. */
  private final Map<Integer, File> paths = new HashMap<Integer, File>();

  public RepositoryImpl(Connection databaseConnection) {
    this(DSL.using(databaseConnection, SQLDialect.H2));
  }
  
  public RepositoryImpl(DSLContext db) {
    this.db = db;
    rootFolder = new FolderRecord();
  }

  public ObjectWithState<ResolvedFile> addFile(FileInformation fileToAdd) throws PictureRepositoryException {
    if (!fileToAdd.getFile().exists()) {
      throw new PictureRepositoryException("File does not exist on disk");
    }
    if (!fileToAdd.isImage()) {
      throw new PictureRepositoryException("The specified path does not denote an image file");
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

      return ObjectWithState.newObject(new ResolvedFileRecord(file, folder, fileToAdd.getFile()));
    } else {
      // The file exists, update the hash if needed.
      if (hash != file.getHash()) {
        // TODO If the hash differs, we might need to clear the reference to the git-metadata file.
        file.setHash(hash);
        file.setSize(new Long(fileToAdd.getFile().length()).intValue());
        file.update();
      }
      return ObjectWithState.existingObject(new ResolvedFileRecord(file, folder, fileToAdd.getFile()));
    }
  }
  
  /**
   * Finds a file in the repository corresponding to a disk file.
   */
  public Optional<ResolvedFile> findFile(File file) {
    if (!file.isFile()) {
      return Optional.empty();
    }
    
    final File normalized = file.getAbsoluteFile();
    
    return getFolderForPath(normalized.getParentFile())
      .flatMap(folderRecord ->
        db.selectFrom(FILE)
          .where(FILE.NAME.eq(file.getName()).and(FILE.FOLDER_ID.eq(folderRecord.getId())))
          .stream()
          .findAny()
          .map(fileRecord -> new ResolvedFileRecord(fileRecord, folderRecord, normalized))
      );
  }
  
  /**
   * Finds all files in the repository matching the given hash.
   */
  public Stream<ResolvedFile> findFiles(String hash) {
    return db.selectFrom(FILE)
      .where(FILE.HASH.eq(hash))
      .fetchStream()
      .map(fileRecord -> new ResolvedFileRecord(fileRecord));
  }

  /**
   * @return a folder for the given path; if the folder or any intermediate folders do not exist in the repository,
   *         then they are created.
   */
  private FolderRecord createFolderForPath(File folder) {
    if (folders.containsKey(folder)) {
      return folders.get(folder);
    } else {
      final String[] elements = getPathElements(folder);
      final FolderRecord target = Arrays.stream(elements).reduce(rootFolder, this::getFolderOrCreate, (a, b) -> b);
      folders.put(folder, target);
      return target;
    }
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
  
  /**
   * @return a <code>FolderRecord</code> corresponding to the given path
   */
  private Optional<FolderRecord> getFolderForPath(File folder) {
    if (folders.containsKey(folder)) {
      return Optional.of(folders.get(folder));
    } else {
      final String[] elements = getPathElements(folder);
      FolderRecord target = rootFolder;
      for(String element : elements) {
        target =
          db.selectFrom(FOLDER)
          .where(FOLDER.NAME.eq(element)
                 .and(target.getId() == null ? FOLDER.PARENT_ID.isNull() : FOLDER.PARENT_ID.eq(target.getId())))
          .fetchOne();
        if (target == null) {
          return Optional.empty();
        }
      }
      folders.put(folder, target);
      return Optional.of(target);
    }
  }
  
  private File getPath(FolderRecord folder) {
    if (paths.containsKey(folder.getId())) {
      return paths.get(folder.getId());
    } else {
      ArrayList<String> elements = new ArrayList<String>();
      elements.add(0, folder.getName());

      FolderRecord target = folder;
      while (target.getParentId() != null) {
        target =
          db.selectFrom(FOLDER)
          .where(FOLDER.ID.eq(target.getParentId()))
          .fetchOne();
        
        if (target == null) {
          throw new RuntimeException("Missing folder elements.");
        }

        elements.add(0, target.getName());
      }
      
      final String path = String.join(File.separator, elements);
      final File file = new File(path);
      paths.put(folder.getId(), file);
      return file;
    }
  }
  
  private String[] getPathElements(File folder) {
    try {
      return folder.getCanonicalPath().split("\\" + File.separator);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Provides information about a file resolved from a FileRecord.
   */
  public class ResolvedFileRecord implements ResolvedFile {
    final FileRecord fileRecord;
    final FolderRecord folderRecord;
    final File containingFolder;
    final File target;

    public ResolvedFileRecord(FileRecord fileRecord, FolderRecord folderRecord, File targetFile) {
      this.fileRecord = fileRecord;
      this.folderRecord = folderRecord;
      target = targetFile;
      containingFolder = targetFile.getParentFile();
    }
    
    public ResolvedFileRecord(FileRecord record) {
      fileRecord = record;
      folderRecord = db.selectFrom(FOLDER)
        .where(FOLDER.ID.eq(record.getFolderId()))
        .fetchOne();
      
      containingFolder = getPath(folderRecord);
      target = new File(containingFolder, record.getName());
    }
    
    public File getFile() {
      return target;
    }
    
    public File getContainingFolder() {
      return containingFolder;
    }
    
    public boolean exists() {
      return target.exists() && target.isFile();
    }
    
    /**
     * @return the hash of the referenced file.
     */
    public String getHash() {
      return fileRecord.getHash();
    }
    
    /**
     * @return the repository that this file was obtained from.
     */
    public PictureRepository getRepository() {
      return RepositoryImpl.this;
    }
  }
}
