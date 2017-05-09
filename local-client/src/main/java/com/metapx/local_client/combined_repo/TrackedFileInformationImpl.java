package com.metapx.local_client.combined_repo;

import java.io.File;

import com.metapx.git_metadata.files.FileRecord;
import com.metapx.local_client.picture_repo.DiskFileInformation;
import com.metapx.local_client.picture_repo.FileInformation;
import com.metapx.local_client.picture_repo.Repository.ResolvedFileRecord;

public class TrackedFileInformationImpl implements TrackedFileInformation {
  private final FileInformation fileInfo;
  private final ResolvedFileRecord resolved;
  private final FileRecord fileRecord;
  
  public TrackedFileInformationImpl(RepositoryStatusFileInformationImpl fileInfo) {
    this.fileInfo = fileInfo;
    this.resolved = fileInfo.resolved.get();
    this.fileRecord = fileInfo.fileRecord.get();
  }
  
  public TrackedFileInformationImpl(ResolvedFileRecord resolved, FileRecord fileRecord) {
    this.fileInfo = new DiskFileInformation(resolved.getFile());
    this.resolved = resolved;
    this.fileRecord = fileRecord;
  }

  @Override
  public File getFile() {
    return fileInfo.getFile();
  }

  @Override
  public String getHash() {
    return fileRecord.getHash();
  }

  @Override
  public boolean isImage() {
    return true;
  }

  @Override
  public String getImageType() {
    return fileRecord.getFiletype();
  }

  @Override
  public int getWidth() {
    return fileRecord.getWidth();
  }

  @Override
  public int getHeight() {
    return fileRecord.getHeight();
  }

  @Override
  public TrackedFileGroup getFileGroup() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FileRecord getFileRecord() {
    return fileRecord;
  }
}
