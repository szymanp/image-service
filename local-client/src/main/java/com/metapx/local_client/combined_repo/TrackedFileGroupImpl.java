package com.metapx.local_client.combined_repo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.metapx.git_metadata.files.FileRecord;
import com.metapx.local_picture_repo.PictureRepository;

public class TrackedFileGroupImpl implements TrackedFileGroup {
  private final String hash;
  private final List<TrackedFileInformation> files;
  private final FileRecord fileRecord;
  
  public TrackedFileGroupImpl(CombinedRepository repos, String hash) {
    this.hash = hash;
    fileRecord = repos.getMetadataRepository().files().findWithKey(getHash())
      .orElseThrow(() -> new RuntimeException("File with hash \"" + hash + "\" is not known by the repository."));
    files = buildFiles(repos.getPictureRepository(), fileRecord, hash);
  }
  
  public TrackedFileGroupImpl(PictureRepository pictureRepo, FileRecord fileRecord) {
    this.hash = fileRecord.getHash();
    this.fileRecord = fileRecord;
    this.files = buildFiles(pictureRepo, fileRecord, this.hash);
  }

  @Override
  public String getHash() {
    return hash;
  }

  @Override
  public Stream<TrackedFileInformation> getFiles() {
    return files.stream();
  }

  @Override
  public FileRecord getFileRecord() {
    return fileRecord;
  }

  @Override
  public Optional<TrackedFileInformation> getValidFile() {
    return getFiles()
      .filter((file) -> file.isValid())
      .findAny();
  }
  
  private List<TrackedFileInformation> buildFiles(PictureRepository pictureRepo, FileRecord fileRecord, String hash) {
    return pictureRepo.findFiles(hash)
      .map(resolved -> new TrackedFileInformationImpl(resolved, fileRecord))
      .collect(Collectors.toList());    
  }
}
