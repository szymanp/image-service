package com.metapx.git_metadata.pictures;

import java.io.File;

import com.metapx.git_metadata.core.OneStringRecord;
import com.metapx.git_metadata.core.TransactionElement;
import com.metapx.git_metadata.core.collections.MappingCollection;
import com.metapx.git_metadata.core.collections.RecordFileCollection;
import com.metapx.git_metadata.core.collections.SkeletonCollection;
import com.metapx.git_metadata.groups.GroupReference;
import com.metapx.git_metadata.references.ReferenceService;
import com.metapx.git_metadata.references.ReferenceService.Message;
import com.metapx.git_metadata.references.ReferenceService.Operation;
import com.metapx.git_metadata.references.Zone;

class MemberGroupCollection extends SkeletonCollection<GroupReference> implements TransactionElement {
  private final TransactionElement transaction;
  private final ReferenceService refService;
  private final PictureReference thisPicture;

  MemberGroupCollection(File file, ReferenceService refService, PictureReference thisPicture) {
    super();

    final RecordFileCollection<OneStringRecord> recordFileCollection =
      new RecordFileCollection<OneStringRecord>(file, OneStringRecord::fromArray, record -> record.getValue());

    final MappingCollection<OneStringRecord, GroupReference> mappingCollection =
      new MappingCollection<OneStringRecord, GroupReference>(
        recordFileCollection,
        record -> new GroupReference(record.getValue()),
        ref -> new OneStringRecord(ref.getObjectId())
      );
    
    this.inner = mappingCollection;
    this.transaction = recordFileCollection.getRecordFile();
    this.refService = refService;
    this.thisPicture = thisPicture;
  }

  @Override
  public void append(GroupReference element) {
    Zone.getCurrent().fork("picture-zone")
      .run(() -> refService.emit(Message.create(thisPicture, Operation.REFERENCE, element)));
    super.append(element);
  }
  @Override
  public void remove(GroupReference element) {
    Zone.getCurrent().fork("picture-zone")
      .run(() -> refService.emit(Message.create(thisPicture, Operation.UNREFERENCE, element)));
    super.remove(element);
  }

  public void commit() throws Exception {
    transaction.commit();
  }
  public void rollback() {
    transaction.rollback();
  }
}
