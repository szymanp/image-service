package com.metapx.git_metadata.core;

public interface TransactionElement {
  void commit() throws Exception;
  void rollback();
}
