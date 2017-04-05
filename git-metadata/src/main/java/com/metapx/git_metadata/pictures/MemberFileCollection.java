package com.metapx.git_metadata.pictures;

import java.io.File;

import com.metapx.git_metadata.core.TransactionControl;
import com.metapx.git_metadata.core.collections.RecordFileCollection;
import com.metapx.git_metadata.files.FileReference;
import com.metapx.git_metadata.references.ReferenceService;
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
    refService.emit(newMessage(element.getFileHash(), Operation.REFERENCE));
    super.append(element);
  }

  public void remove(MemberFile element) {
    refService.emit(newMessage(element.getFileHash(), Operation.UNREFERENCE));
    super.remove(element);
  }

  private ReferenceService.Message newMessage(String fileHash, Operation op) {
    final PictureReference pictureRef = new PictureReference(pictureHash);
    final ReferenceService.MessageBuilder builder = ReferenceService.newMessageBuilder(pictureRef, op);
    builder.references(new FileReference(fileHash));

    return builder.build();
  }
}
