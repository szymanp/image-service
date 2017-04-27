package com.metapx.git_metadata.files;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.metapx.git_metadata.core.TransactionControl;
import com.metapx.git_metadata.core.collections.KeyedCollection;
import com.metapx.git_metadata.pictures.PictureReference;
import com.metapx.git_metadata.references.ReferenceService;
import com.metapx.git_metadata.references.ReferenceService.Operation;

public class FileService {
  private final FileCollection coll;
  private final ReferenceService refService;

  public FileService(File root, TransactionControl transaction, ReferenceService refService) {
    this.refService = refService;
    coll = new FileCollection(root);
    transaction.addElementToTransaction(coll.files);

    refService.messages(PictureReference.class, FileReference.class)
      .subscribe(m -> {
        if (m.operation() == Operation.REFERENCE) {
          this.addPictureReference(m.target().getObjectId(), m.source().getObjectId());
        } else if (m.operation() == Operation.UNREFERENCE) {
          this.removePictureReference(m.target().getObjectId(), m.source().getObjectId());
        }
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
        throw new RuntimeException("File " + fileHash + " is already associated with picture " + fileRecord.getPictureId());
      }

      fileRecord.setPictureId(pictureHash);
      coll.update(fileRecord);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void removePictureReference(String fileHash, String pictureHash) {
    try {
      final FileRecord fileRecord = getFileRecordOrThrow(fileHash);
      if (!fileRecord.getPictureId().equals(pictureHash)) {
        throw new RuntimeException("File " + fileHash + " is not associated with picture " + pictureHash);
      }

      fileRecord.setPictureId("");
      coll.update(fileRecord);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
