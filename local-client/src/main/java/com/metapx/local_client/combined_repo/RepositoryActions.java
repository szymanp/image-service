package com.metapx.local_client.combined_repo;

import java.io.IOException;
import java.util.Optional;

import com.metapx.git_metadata.core.IntegrityException;
import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.git_metadata.files.FileRecord;
import com.metapx.git_metadata.groups.Group;
import com.metapx.git_metadata.pictures.MemberFile;
import com.metapx.git_metadata.pictures.Picture;
import com.metapx.git_metadata.pictures.PictureReference;
import com.metapx.local_client.cli.Configuration;
import com.metapx.local_client.cli.DeviceFolders;
import com.metapx.local_client.commands.parsers.GroupPath;
import com.metapx.local_picture_repo.FileInformation;
import com.metapx.local_picture_repo.ObjectWithState;
import com.metapx.local_picture_repo.ObjectWithState.State;
import com.metapx.local_picture_repo.PictureRepository;
import com.metapx.local_picture_repo.PictureRepositoryException;
import com.metapx.local_picture_repo.ResolvedFile;

public class RepositoryActions {
  final private PictureRepository pictures;
  final private MetadataRepository metadata;
  
  final private DeviceFolders deviceFolders;

  public RepositoryActions(CombinedRepository repos, Configuration conf) {
    this.pictures = repos.getPictureRepository();
    this.metadata = repos.getMetadataRepository();
    
    deviceFolders = new DeviceFolders(conf, metadata);
  }

  public TrackedFileInformation addFile(FileInformation file) throws IOException, PictureRepositoryException {
    final ObjectWithState<ResolvedFile> resolved = pictures.addFile(file);
    final ObjectWithState<FileRecord> fileRecord = addFileToMetadataRepository(file);
    return new TrackedFileInformationImpl(resolved.get(), fileRecord.get());
  }

  public TrackedFileInformation addFileAsPicture(FileInformation file) throws IOException, PictureRepositoryException {
    final ObjectWithState<ResolvedFile> resolved = pictures.addFile(file);
    final ObjectWithState<FileRecord> fileRecord = addFileToMetadataRepository(file);
    FileRecord latestFileRecord = fileRecord.get();

    if (fileRecord.state() == State.NEW && fileRecord.get().getPictureId().equals("")) {
      final Picture picture = metadata.pictureApi().create();
      picture.files().append(new MemberFile(file.getHash(), Picture.Role.ROOT));
      picture.groups().append(deviceFolders.getDeviceGroup().getReference());
      picture.groups().append(deviceFolders.getFolder(file.getFile().getParentFile()).getReference());
      metadata.pictureApi().pictures().update(picture);
      latestFileRecord = metadata.files().findWithKey(fileRecord.get().getHash())
        .orElseThrow(() -> new IntegrityException("The recently added file is not available."));
    }
    
    return new TrackedFileInformationImpl(resolved.get(), latestFileRecord);
  }
  
  public void addFileToGroup(TrackedFileInformation file, GroupPath path) {
    final Optional<Group> group = metadata.groupApi().findGroupByPath(path.getParts());
    final Optional<PictureReference> picture = PictureReference.create(file.getFileRecord().getPictureId());
    if (group.isPresent() && picture.isPresent()) {
      group.get().pictures().append(picture.get());
    } else if (!group.isPresent()) {
      throw new ParameterException("Group \"" + path.getPath() + "\" does not exist.");
    } else if (!picture.isPresent()) {
      throw new IntegrityException("The file is not associated with any picture.");
    }
  }
  
  /**
   * Adds a file to the metadata repository.
   * @return `true` if the file was added, or `false` if it was already registered in the repository.
   */
  private ObjectWithState<FileRecord> addFileToMetadataRepository(FileInformation file) throws IOException {
    if (!file.isImage()) {
      throw new ParameterException("File is not an image");
    }
    final Optional<FileRecord> fileRecordOpt = metadata.files().findWithKey(file.getHash());
    if (!fileRecordOpt.isPresent()) {
      FileRecord fileRecord = new FileRecord();
      fileRecord.setDefaultFilename(file.getFile().getName());
      fileRecord.setFiletype(file.getImageType());
      fileRecord.setHash(file.getHash());
      fileRecord.setHeight(file.getHeight());
      fileRecord.setWidth(file.getWidth());
      fileRecord.setSize(new Long(file.getFile().length()).intValue());
      metadata.files().append(fileRecord);

      return ObjectWithState.newObject(fileRecord);
    } else {
      return ObjectWithState.existingObject(fileRecordOpt.get());
    }
  }
}
