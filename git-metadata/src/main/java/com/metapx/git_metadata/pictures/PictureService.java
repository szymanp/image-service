package com.metapx.git_metadata.pictures;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.metapx.git_metadata.core.HashPath;
import com.metapx.git_metadata.core.HashPathTransactionElement;
import com.metapx.git_metadata.core.IdService;
import com.metapx.git_metadata.core.TransactionControl;
import com.metapx.git_metadata.core.UpdatableRecordFile;

public class PictureService {
  private final HashPathTransactionElement<UpdatableRecordFile<Picture.FileLine>> pictures;
  private final IdService idService;

  public PictureService(File root, TransactionControl transaction, IdService idService) {
    this.idService = idService;
    pictures = new HashPathTransactionElement<UpdatableRecordFile<Picture.FileLine>>(new HashPath(root), target -> {
      return new UpdatableRecordFile<Picture.FileLine>(target.getFile(), fields -> Picture.FileLine.fromArray(fields));
    });
    transaction.addElementToTransaction(pictures);
  }

  public Optional<Picture> find(String hash) throws IOException {
    return pictures.getIfExists(hash).map(file -> {
      final Picture picture = new Picture();
      picture.setHash(hash);
      try {
        file.all().forEach(line -> picture.getFiles().add(line));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return picture;
    });
  }

  public Picture create() {
    final Picture picture = new Picture();
    picture.setHash(idService.createId("picture"));
    return picture;
  }

  public void update(Picture picture) throws IOException {
    final UpdatableRecordFile<Picture.FileLine> recordFile = pictures.get(picture.getHash());
    recordFile.clear();
    picture.getFiles().forEach(file -> {
      try {
        recordFile.append(file);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
