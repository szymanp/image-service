package com.metapx.local_client.combined_repo;

import java.io.File;
import java.util.Optional;

import com.metapx.git_metadata.files.FileRecord;
import com.metapx.local_client.picture_repo.FileInformation;
import com.metapx.local_client.picture_repo.Repository.ResolvedFileRecord;

public class RepositoryStatusFileInformationImpl implements RepositoryStatusFileInformation {
  private final FileInformation fileInfo;
  final Optional<ResolvedFileRecord> resolved;
  final Optional<FileRecord> fileRecord;

  public RepositoryStatusFileInformationImpl(CombinedRepository repos, FileInformation fileInfo) {
    this.fileInfo = fileInfo;
    
    resolved = repos.getPictureRepository().findFile(fileInfo.getFile());
    fileRecord = repos.getMetadataRepository().files().findWithKey(getHash());
  }

  @Override
  public File getFile() {
    return fileInfo.getFile();
  }

  @Override
  public String getHash() {
    return resolved
      .map(resolved -> resolved.getHash())
      .orElseGet(() -> fileInfo.getHash());
  }

  @Override
  public boolean isImage() {
    return resolved.isPresent() ? true : fileInfo.isImage();
  }

  @Override
  public String getImageType() {
    return fileRecord
      .map(fileRecord -> fileRecord.getFiletype())
      .orElseGet(() -> fileInfo.getImageType());
  }

  @Override
  public int getWidth() {
    return fileRecord
      .map(fileRecord -> fileRecord.getWidth())
      .orElseGet(() -> fileInfo.getWidth());
  }

  @Override
  public int getHeight() {
    return fileRecord
      .map(fileRecord -> fileRecord.getHeight())
      .orElseGet(() -> fileInfo.getHeight());
  }

  @Override
  public boolean isKnown() {
    return fileRecord.isPresent();
  }

  @Override
  public boolean isTracked() {
    return resolved.isPresent();
  }

  @Override
  public Optional<TrackedFileInformation> getTrackedFile() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<TrackedFileGroup> getFileGroup() {
    // TODO Auto-generated method stub
    return null;
  }
}
