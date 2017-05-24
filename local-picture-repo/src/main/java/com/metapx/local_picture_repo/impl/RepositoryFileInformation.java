package com.metapx.local_picture_repo.impl;

import java.io.File;

import com.metapx.local_picture_repo.FileInformation;
import com.metapx.local_picture_repo.ResolvedFile;

public class RepositoryFileInformation implements FileInformation {
  final ResolvedFile resolvedFile;
  
  public RepositoryFileInformation(ResolvedFile resolvedFile) {
    this.resolvedFile = resolvedFile;
  }

  @Override
  public File getFile() {
    return resolvedFile.getFile();
  }

  @Override
  public String getHash() {
    return resolvedFile.getHash();
  }

  @Override
  public boolean isImage() {
    return true;
  }

  @Override
  public String getImageType() {
    return resolvedFile.getImageType();
  }

  @Override
  public int getWidth() {
    return resolvedFile.getWidth();
  }

  @Override
  public int getHeight() {
    return resolvedFile.getWidth();
  } 
}
