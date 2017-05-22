package com.metapx.local_picture_repo.scaling;

import java.io.File;

import com.metapx.local_picture_repo.PictureRepository;
import com.metapx.local_picture_repo.ResolvedFile;
import com.metapx.local_picture_repo.SamplePictures;
import com.metapx.local_picture_repo.impl.DiskFileInformation;

public class SampleResolvedFile implements ResolvedFile {
  final DiskFileInformation target;
  
  public SampleResolvedFile(String name) {
    target = new DiskFileInformation(SamplePictures.getFile(name));
  }

  @Override
  public File getFile() {
    return target.getFile();
  }

  @Override
  public File getContainingFolder() {
    return target.getFile().getParentFile();
  }

  @Override
  public boolean exists() {
    return true;
  }

  @Override
  public String getHash() {
    return target.getHash();
  }

  @Override
  public PictureRepository getRepository() {
    throw new RuntimeException("Not implemented.");
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public int getWidth() {
    return target.getWidth();
  }

  @Override
  public int getHeight() {
    return target.getHeight();
  }

  @Override
  public String getImageType() {
    return target.getImageType();
  }

}
