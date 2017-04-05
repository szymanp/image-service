package com.metapx.git_metadata.core;

import java.io.File;

public class MockIdService extends IdService {
  public String nextId;

  public MockIdService(File listFile, TransactionControl transaction) {
    super(listFile, transaction);
  }

  public String createId(String idType) {
    return nextId;
  }
}
