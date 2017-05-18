package com.metapx.local_client.combined_repo;

import java.io.File;

import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.local_picture_repo.PictureRepository;
import com.metapx.local_picture_repo.picture_repo.DiskFileInformation;

/**
 * A container for picture and metadata repository instances. 
 */
public class CombinedRepository {
  final private PictureRepository pictureRepository;
  final private MetadataRepository metadataRepository;
  
  public CombinedRepository(PictureRepository pictureRepository, MetadataRepository metadataRepository) {
    this.pictureRepository = pictureRepository;
    this.metadataRepository = metadataRepository;
  }
  
  public PictureRepository getPictureRepository() {
    return pictureRepository;
  }

  public MetadataRepository getMetadataRepository() {
    return metadataRepository;
  }
  
  public RepositoryStatusFileInformation getFile(File file) {
    return new RepositoryStatusFileInformationImpl(this, new DiskFileInformation(file));
  }
}
