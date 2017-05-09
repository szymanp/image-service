package com.metapx.local_client.combined_repo;

import java.io.File;

import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.local_client.picture_repo.DiskFileInformation;
import com.metapx.local_client.picture_repo.Repository;

/**
 * A container for picture and metadata repository instances. 
 */
public class CombinedRepository {
  final private Repository pictureRepository;
  final private MetadataRepository metadataRepository;
  
  public CombinedRepository(Repository pictureRepository, MetadataRepository metadataRepository) {
    this.pictureRepository = pictureRepository;
    this.metadataRepository = metadataRepository;
  }
  
  public Repository getPictureRepository() {
    return pictureRepository;
  }

  public MetadataRepository getMetadataRepository() {
    return metadataRepository;
  }
  
  public RepositoryStatusFileInformation getFile(File file) {
    return new RepositoryStatusFileInformationImpl(this, new DiskFileInformation(file));
  }
}
