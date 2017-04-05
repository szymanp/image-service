package com.metapx.git_metadata.files;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.metapx.git_metadata.core.TransactionControl;
import com.metapx.git_metadata.core.collections.KeyedCollection;
import com.metapx.git_metadata.pictures.Picture;
import com.metapx.git_metadata.pictures.PictureReference;
import com.metapx.git_metadata.references.ReferenceService;
import com.metapx.git_metadata.references.ReferenceService.Operation;
import com.metapx.git_metadata.references.ReferenceService.OperationException;

public class FileService {
  private final FileCollection coll;
  private final ReferenceService refService;

  public FileService(File root, TransactionControl transaction, ReferenceService refService) {
    this.refService = refService;
    coll = new FileCollection(root);
    transaction.addElementToTransaction(coll.files);

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

  public KeyedCollection<String, FileRecord> files() {
    return coll;
  }

  private FileRecord getFileRecordOrThrow(String hash) throws IOException {
    final Optional<FileRecord> fileRecordOpt = coll.findWithKey(hash);
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
      coll.update(fileRecord);
    } catch (Exception e) {
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
      coll.update(fileRecord);
    } catch (Exception e) {
      throw new OperationException(e);
    }
  }
}
