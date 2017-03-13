package com.metapx.local_client.cli;

import java.io.IOException;
import java.sql.Connection;

import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.git_metadata.files.FileRecord;
import com.metapx.local_client.picture_repo.FileInformation;
import com.metapx.local_client.picture_repo.Repository;

public class RepositoryActions {
  final private Repository pictures;
  final private MetadataRepository metadata;
  final private Connection conn;

  public RepositoryActions(Connection conn, Repository pictureRepository, MetadataRepository metadataRepository) {
    this.pictures = pictureRepository;
    this.metadata = metadataRepository;
    this.conn = conn;
  }

  /**
   * Adds a file to the metadata repository.
   * @return `true` if the file was added, or `false` if it was already registered in the repository.
   */
  public boolean addFileToMetadataRepository(FileInformation file) throws IOException {
    if (!file.isImage()) {
      throw new RuntimeException("File is not an image");
    }
    if (!metadata.files().find(file.getHash()).isPresent()) {
      FileRecord fileRecord = new FileRecord();
      fileRecord.setDefaultFilename(file.getFile().getName());
      fileRecord.setFiletype(file.getImageType());
      fileRecord.setHash(file.getHash());
      fileRecord.setHeight(file.getHeight());
      fileRecord.setWidth(file.getWidth());
      fileRecord.setSize(new Long(file.getFile().length()).intValue());
      metadata.files().create(fileRecord);

      return true;
    } else {
      return false;
    }
  }

  public void addFile(FileInformation file) throws IOException, Repository.RepositoryException {
    pictures.addFile(file);
    addFileToMetadataRepository(file);
  }

  public void commit() throws Exception {
    conn.commit();
    metadata.commit();
  }
}