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
    final String uuid = UUID.randomUUID().toString();
    final String id = uuid.substring(0, 8)
      + uuid.substring(9, 13)
      + uuid.substring(14, 18)
      + uuid.substring(19, 23)
      + uuid.substring(24, 36);
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
