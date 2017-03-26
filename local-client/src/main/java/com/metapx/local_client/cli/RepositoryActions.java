package com.metapx.local_client.cli;

import java.io.IOException;
import java.sql.Connection;
import java.util.Optional;

import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.git_metadata.files.FileRecord;
import com.metapx.git_metadata.pictures.Picture;
import com.metapx.local_client.picture_repo.FileInformation;
import com.metapx.local_client.picture_repo.ObjectWithState;
import com.metapx.local_client.picture_repo.Repository;
import com.metapx.local_client.picture_repo.ObjectWithState.State;

public class RepositoryActions {
  final private Repository pictures;
  final private MetadataRepository metadata;
  final private Connection conn;

  public RepositoryActions(Connection conn, Repository pictureRepository, MetadataRepository metadataRepository) {
    this.pictures = pictureRepository;
    this.metadata = metadataRepository;
    this.conn = conn;
  }

  public void addFile(FileInformation file) throws IOException, Repository.RepositoryException {
    pictures.addFile(file);
    addFileToMetadataRepository(file);
  }

  public void addFileAsPicture(FileInformation file) throws IOException, Repository.RepositoryException {
    pictures.addFile(file);
    final ObjectWithState<FileRecord> fileRecord = addFileToMetadataRepository(file);

    if (fileRecord.state() == State.NEW && fileRecord.get().getPictureId().equals("")) {
      final Picture picture = metadata.pictures().create();
      picture.getFiles().add(new Picture.FileLine(file.getHash(), Picture.Role.ROOT));
      metadata.pictures().update(picture);
    }
  }

  public void commit() throws Exception {
    conn.commit();
    metadata.commit();
  }

  /**
   * Adds a file to the metadata repository.
   * @return `true` if the file was added, or `false` if it was already registered in the repository.
   */
  private ObjectWithState<FileRecord> addFileToMetadataRepository(FileInformation file) throws IOException {
    if (!file.isImage()) {
      throw new RuntimeException("File is not an image");
    }
    final Optional<FileRecord> fileRecordOpt = metadata.files().find(file.getHash());
    if (!fileRecordOpt.isPresent()) {
      FileRecord fileRecord = new FileRecord();
      fileRecord.setDefaultFilename(file.getFile().getName());
      fileRecord.setFiletype(file.getImageType());
      fileRecord.setHash(file.getHash());
      fileRecord.setHeight(file.getHeight());
      fileRecord.setWidth(file.getWidth());
      fileRecord.setSize(new Long(file.getFile().length()).intValue());
      metadata.files().create(fileRecord);

      return ObjectWithState.newObject(fileRecord);
    } else {
      return ObjectWithState.existingObject(fileRecordOpt.get());
    }
  }
}
