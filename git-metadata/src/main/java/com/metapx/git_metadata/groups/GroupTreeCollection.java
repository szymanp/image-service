package com.metapx.git_metadata.groups;

import com.metapx.git_metadata.core.TransactionControl;
import com.metapx.git_metadata.core.collections.RecordFileCollection;

import java.io.File;

class GroupTreeCollection extends RecordFileCollection<GroupTreeRecord> {
  GroupTreeCollection(File treeFile, TransactionControl transaction) {
    super(treeFile, GroupTreeRecord::fromArray, record -> record.getGroupHash(), 1);
    transaction.addElementToTransaction(getRecordFile());
  }
}
