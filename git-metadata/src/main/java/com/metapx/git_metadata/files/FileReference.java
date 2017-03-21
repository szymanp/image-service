package com.metapx.git_metadata.files;

import com.metapx.git_metadata.references.Reference;

public class FileReference extends Reference<String> {
  public FileReference(String hash) {
    super(FileRecord.class, hash);
  }
}
