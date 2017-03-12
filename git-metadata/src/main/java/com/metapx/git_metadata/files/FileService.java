package com.metapx.git_metadata.files;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.metapx.git_metadata.core.HashPath;
import com.metapx.git_metadata.core.HashPathTransactionElement;
import com.metapx.git_metadata.core.TransactionControl;
import com.metapx.git_metadata.core.UpdatableRecordFile;

public class FileService {
  private final HashPathTransactionElement<UpdatableRecordFile<FileRecord>> files;

  public FileService(File root, TransactionControl transaction) {
    files = new HashPathTransactionElement<UpdatableRecordFile<FileRecord>>(new HashPath(root), target -> {
      return new UpdatableRecordFile<FileRecord>(target.getFile(), fields -> FileRecord.fromArray(fields));
    });
    transaction.addElementToTransaction(files);
  }

  /**
   * Reads a file record for the given file, if it exists.
   */
  public Optional<FileRecord> read(String hash) throws IOException {
    return files.get(hash).get(0);
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
}
