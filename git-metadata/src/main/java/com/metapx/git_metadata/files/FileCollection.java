package com.metapx.git_metadata.files;

import com.metapx.git_metadata.core.collections.KeyedCollection;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.metapx.git_metadata.core.HashPath;
import com.metapx.git_metadata.core.HashPathTransactionElement;
import com.metapx.git_metadata.core.UpdatableRecordFile;
import com.metapx.git_metadata.core.collections.Collection;

class FileCollection implements KeyedCollection<String, FileRecord>, Collection<FileRecord> {
  final HashPathTransactionElement<UpdatableRecordFile<FileRecord>> files;

  FileCollection(File root) {
    files = new HashPathTransactionElement<UpdatableRecordFile<FileRecord>>(new HashPath(root), target -> {
      return new UpdatableRecordFile<FileRecord>(target.getFile(), fields -> FileRecord.fromArray(fields));
    });
  }
  
  public void append(FileRecord record) {
    final UpdatableRecordFile<FileRecord> recordFile = files.get(record.getHash());
    if (recordFile.get(0).isPresent()) {
      throw new RuntimeException("File already exists");
    } else {
      recordFile.set(0, record);
    }
  }

  public void update(FileRecord record) {
    final UpdatableRecordFile<FileRecord> recordFile = files.get(record.getHash());
    if (!recordFile.get(0).isPresent()) {
      throw new RuntimeException("File does not exist");
    }
    recordFile.set(0, record);
  }

  public void remove(FileRecord record) {
    throw new RuntimeException("Not implemented");
  }

  public boolean contains(FileRecord record) {
    return files.getIfExists(record.getHash()).isPresent();
  }

  public Optional<FileRecord> findWithKey(String hash) {
    final Optional<FileRecord> result = files.get(hash).get(0);

    // The record reader does not set a hash by itself. We need to do it here.
    if (result.isPresent() && result.get().getHash() == null) {
      result.get().setHash(hash);
    }

    return result;
  }

  public List<FileRecord> list() {
    throw new RuntimeException("Not implemented");
  }

  public Stream<FileRecord> stream() {
    throw new RuntimeException("Not implemented");
  }
}
