package com.metapx.git_metadata.files;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.metapx.git_metadata.core.HashPath;
import com.metapx.git_metadata.core.HashPathTransactionElement;
import com.metapx.git_metadata.core.TransactionControl;
import com.metapx.git_metadata.core.UpdatableRecordFile;
import com.metapx.git_metadata.pictures.Picture;
import com.metapx.git_metadata.pictures.PictureReference;
import com.metapx.git_metadata.references.ReferenceService;
import com.metapx.git_metadata.references.ReferenceService.Operation;
import com.metapx.git_metadata.references.ReferenceService.OperationException;

public class FileService {
  private final HashPathTransactionElement<UpdatableRecordFile<FileRecord>> files;
  private final ReferenceService refService;

  public FileService(File root, TransactionControl transaction, ReferenceService refService) {
    this.refService = refService;
    files = new HashPathTransactionElement<UpdatableRecordFile<FileRecord>>(new HashPath(root), target -> {
      return new UpdatableRecordFile<FileRecord>(target.getFile(), fields -> FileRecord.fromArray(fields));
    });
    transaction.addElementToTransaction(files);

    refService.register(Picture.class, Operation.REFERENCE, message -> {
      message.getReferences(FileReference.class).forEach(fileRef -> {
        final PictureReference pictureRef = (PictureReference) message.getOrigin();
        this.addPictureReference(fileRef.getObjectId(), pictureRef.getObjectId());
      });
    });

    refService.register(Picture.class, Operation.UNREFERENCE, message -> {
      message.getReferences(FileReference.class).forEach(fileRef -> {
        final PictureReference pictureRef = (PictureReference) message.getOrigin();
        this.removePictureReference(fileRef.getObjectId(), pictureRef.getObjectId());
      });
    });
  }

  /**
   * Reads a file record for the given file, if it exists.
   */
  public Optional<FileRecord> find(String hash) throws IOException {
    final Optional<FileRecord> result = files.get(hash).get(0);

    // The record reader does not set a hash by itself. We need to do it here.
    if (result.isPresent() && result.get().getHash() == null) {
      result.get().setHash(hash);
    }

    return result;
  }

  public void create(FileRecord record) throws IOException {
    final UpdatableRecordFile<FileRecord> recordFile = files.get(record.getHash());
    if (recordFile.get(0).isPresent()) {
      throw new IOException("File already exists");
    } else {
      recordFile.set(0, record);
    }
  }

  public void update(FileRecord record) throws IOException {
    final UpdatableRecordFile<FileRecord> recordFile = files.get(record.getHash());
    if (!recordFile.get(0).isPresent()) {
      throw new IOException("File does not exist");
    }
    recordFile.set(0, record);
  }

  private FileRecord getFileRecordOrThrow(String hash) throws IOException {
    final Optional<FileRecord> fileRecordOpt = find(hash);
    if (!fileRecordOpt.isPresent()) {
      throw new IOException("File does not exist: " + hash);
    }
    return fileRecordOpt.get();
  }

  private void addPictureReference(String fileHash, String pictureHash) {
    try {
      final FileRecord fileRecord = getFileRecordOrThrow(fileHash);
      if (!fileRecord.getPictureId().equals("") && !fileRecord.getPictureId().equals(pictureHash)) {
        throw new OperationException("File " + fileHash + " is already associated with picture " + fileRecord.getPictureId());
      }

      fileRecord.setPictureId(pictureHash);
      update(fileRecord);
    } catch (IOException e) {
      throw new OperationException(e);
    }
  }

  private void removePictureReference(String fileHash, String pictureHash) {
    try {
      final FileRecord fileRecord = getFileRecordOrThrow(fileHash);
      if (!fileRecord.getPictureId().equals(pictureHash)) {
        throw new OperationException("File " + fileHash + " is not associated with picture " + pictureHash);
      }

      fileRecord.setPictureId("");
      update(fileRecord);
    } catch (IOException e) {
      throw new OperationException(e);
    }
  }
}
