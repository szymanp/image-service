package com.metapx.git_metadata.core;

interface TransactionElement {
  void commit() throws Exception;
  void rollback();
}
