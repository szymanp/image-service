package com.metapx.local_client.combined_repo;

import java.io.File;
import java.util.Optional;

import com.metapx.git_metadata.files.FileRecord;
import com.metapx.local_picture_repo.FileInformation;
import com.metapx.local_picture_repo.ResolvedFile;

public class RepositoryStatusFileInformationImpl implements RepositoryStatusFileInformation {
  private final FileInformation fileInfo;
  private final CombinedRepository repos;
  final Optional<ResolvedFile> resolved;
  final Optional<FileRecord> fileRecord;
  
  private Optional<TrackedFileInformation> trackedFile;
  private Optional<TrackedFileGroup> trackedFileGroup;

  public RepositoryStatusFileInformationImpl(CombinedRepository repos, FileInformation fileInfo) {
    this.repos = repos;
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
    if (trackedFile == null) {
      if (resolved.isPresent() && fileRecord.isPresent()) {
        trackedFile = Optional.of(new TrackedFileInformationImpl(resolved.get(), fileRecord.get()));
      } else {
        trackedFile = Optional.empty();
      }
    }
    return trackedFile;
  }

  @Override
  public Optional<TrackedFileGroup> getFileGroup() {
    if (trackedFileGroup == null) {
      if (fileRecord.isPresent()) {
        trackedFileGroup = Optional.of((TrackedFileGroup)new TrackedFileGroupImpl(repos.getPictureRepository(), fileRecord.get()));
      } else {
        trackedFileGroup = Optional.empty();
      }
    }
    return trackedFileGroup;
  }
}
