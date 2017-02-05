package com.metapx.git_metadata.core;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class IdService {
  private final RecordFile<RecordFile.StringRecord> file;

  IdService(File listFile, TransactionControl transaction) {
    file = new RecordFile<RecordFile.StringRecord>(
      listFile,
      new RecordFile.StringRecord.Reader()
    );
    transaction.addElementToTransaction(file);
  }

  public String createId(String idType) {
    final String id = UUID.randomUUID().toString();
    try {
      if (file.findWithKey(id).isPresent()) {
        throw new RuntimeException("Collision with existing key detected");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    file.append(new RecordFile.StringRecord(new String[] { id, idType }));

    return id;
  }
}
