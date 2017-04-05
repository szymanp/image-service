package com.metapx.git_metadata.pictures;

import java.io.File;

import com.metapx.git_metadata.core.IdService;
import com.metapx.git_metadata.core.TransactionControl;
import com.metapx.git_metadata.core.collections.KeyedCollection;
import com.metapx.git_metadata.references.ReferenceService;

public class PictureService {
  final ReferenceService refService;
  final PictureCollection coll;
  private final IdService idService;

  public PictureService(File root, TransactionControl transaction, IdService idService, ReferenceService refService) {
    this.idService = idService;
    this.refService = refService;
    this.coll = new PictureCollection(root, target -> new Picture(this, target));
    transaction.addElementToTransaction(this.coll.pictures);
  }

  public Picture create() {
    return new Picture(this, idService.createId("picture"));
  }

  public KeyedCollection<String, Picture> pictures() {
    return coll;
  }
}
