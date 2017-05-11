package com.metapx.git_metadata.pictures;

import java.util.Optional;

import com.metapx.git_metadata.references.Reference;

public class PictureReference extends Reference<String> {

  public static Optional<PictureReference> create(String hash) {
    if (hash == null || hash.equals("")) {
      return Optional.empty();
    } else {
      return Optional.of(new PictureReference(hash));
    }
  }
  
  public PictureReference(String hash) {
    super(Picture.class, hash);
  }
}
