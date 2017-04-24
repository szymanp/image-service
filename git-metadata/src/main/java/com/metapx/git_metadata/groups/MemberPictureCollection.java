package com.metapx.git_metadata.groups;

import com.metapx.git_metadata.core.OneStringRecord;
import com.metapx.git_metadata.core.TransactionElement;
import com.metapx.git_metadata.core.collections.MappingCollection;
import com.metapx.git_metadata.core.collections.RecordFileCollection;
import com.metapx.git_metadata.core.collections.SkeletonCollection;
import com.metapx.git_metadata.pictures.PictureReference;

import java.io.File;

class MemberPictureCollection extends SkeletonCollection<PictureReference> implements TransactionElement {
  private final TransactionElement transaction;

  MemberPictureCollection(File recordFile) {
    super();

    final RecordFileCollection<OneStringRecord> recordFileCollection =
      new RecordFileCollection<OneStringRecord>(recordFile, OneStringRecord::fromArray, record -> record.getValue());
    final MappingCollection<OneStringRecord, PictureReference> mappingCollection =
      new MappingCollection<OneStringRecord, PictureReference>(
        recordFileCollection,
        record -> new PictureReference(record.getValue()),
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
