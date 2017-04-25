package com.metapx.git_metadata.pictures;

import java.io.File;

import com.metapx.git_metadata.core.OneStringRecord;
import com.metapx.git_metadata.core.TransactionElement;
import com.metapx.git_metadata.core.collections.MappingCollection;
import com.metapx.git_metadata.core.collections.RecordFileCollection;
import com.metapx.git_metadata.core.collections.SkeletonCollection;
import com.metapx.git_metadata.groups.GroupReference;

class MemberGroupCollection extends SkeletonCollection<GroupReference> implements TransactionElement {
  private final TransactionElement transaction;

  MemberGroupCollection(File file) {
    super();

    final RecordFileCollection<OneStringRecord> recordFileCollection =
      new RecordFileCollection<OneStringRecord>(file, OneStringRecord::fromArray, record -> record.getValue());

    final MappingCollection<OneStringRecord, GroupReference> mappingCollection =
      new MappingCollection<OneStringRecord, GroupReference>(
        recordFileCollection,
        record -> new GroupReference(record.getValue()),
        ref -> new OneStringRecord(ref.getObjectId())
      );
    
    inner = mappingCollection;
    transaction = recordFileCollection.getRecordFile();
  }

  public void commit() throws Exception {
    transaction.commit();
  }
  public void rollback() {
    transaction.rollback();
  }
}
