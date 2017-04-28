package com.metapx.git_metadata.groups;

import com.metapx.git_metadata.core.OneStringRecord;
import com.metapx.git_metadata.core.TransactionElement;
import com.metapx.git_metadata.core.collections.MappingCollection;
import com.metapx.git_metadata.core.collections.RecordFileCollection;
import com.metapx.git_metadata.core.collections.SkeletonCollection;
import com.metapx.git_metadata.pictures.PictureReference;
import com.metapx.git_metadata.references.ReferenceService;
import com.metapx.git_metadata.references.ReferenceService.Message;
import com.metapx.git_metadata.references.ReferenceService.Operation;

import java.io.File;

class MemberPictureCollection extends SkeletonCollection<PictureReference> implements TransactionElement {
  private final TransactionElement transaction;
  private final ReferenceService refService;
  private final GroupReference thisGroup;

  MemberPictureCollection(File recordFile, ReferenceService refService, GroupReference thisGroup) {
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
    this.refService = refService;
    this.thisGroup = thisGroup;
  }

  @Override
  public void append(PictureReference element) {
    refService.emit(Message.create(thisGroup, Operation.REFERENCE, element));
    super.append(element);
  }

  @Override
  public void remove(PictureReference element) {
    refService.emit(Message.create(thisGroup, Operation.UNREFERENCE, element));
    super.remove(element);
  }

  public void commit() throws Exception {
    transaction.commit();
  }
  public void rollback() {
    transaction.rollback();
  }
}
