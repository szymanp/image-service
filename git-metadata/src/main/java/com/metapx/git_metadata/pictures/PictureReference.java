package com.metapx.git_metadata.pictures;

import com.metapx.git_metadata.references.Reference;

public class PictureReference extends Reference<String> {
  public PictureReference(String hash) {
    super(Picture.class, hash);
  }
}
