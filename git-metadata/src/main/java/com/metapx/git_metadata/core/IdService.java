package com.metapx.git_metadata.core;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class IdService {
  private final RecordFile<StringRecord> file;

  public IdService(File listFile, TransactionControl transaction) {
    file = new AppendableRecordFile<StringRecord>(
      listFile,
      new StringRecord.Reader()
    );
    transaction.addElementToTransaction(file);
  }

  public String createId(String idType) {
    final String id = UUID.randomUUID().toString();
    try {
      if (file.findWithKey(id).isPresent()) {
        throw new RuntimeException("Collision with existing key detected");
      }

      file.append(new StringRecord(new String[] { id, idType }));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return id;
  }
}
