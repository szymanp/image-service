package com.metapx.git_metadata.pictures;

import java.io.File;

import com.metapx.git_metadata.core.TransactionControl;
import com.metapx.git_metadata.core.collections.RecordFileCollection;
import com.metapx.git_metadata.files.FileReference;
import com.metapx.git_metadata.references.ReferenceService;
import com.metapx.git_metadata.references.ReferenceService.Message;
import com.metapx.git_metadata.references.ReferenceService.Operation;

class MemberFileCollection extends RecordFileCollection<MemberFile> {
  private final ReferenceService refService;
  private final String pictureHash;

  MemberFileCollection(String pictureHash, File file, TransactionControl transaction, ReferenceService refService) {
    super(file, fields -> MemberFile.fromArray(fields), memberFile -> memberFile.getFileHash());
    transaction.addElementToTransaction(getRecordFile());
    this.pictureHash = pictureHash;
    this.refService = refService;
  }

  public void append(MemberFile element) {
    refService.emit(Message.create(new PictureReference(pictureHash), Operation.REFERENCE, new FileReference(element.getFileHash())));
    super.append(element);
  }

  public void remove(MemberFile element) {
    refService.emit(Message.create(new PictureReference(pictureHash), Operation.UNREFERENCE, new FileReference(element.getFileHash())));
    super.remove(element);
  }
}
