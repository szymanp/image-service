package com.metapx.local_client.combined_repo;

import java.io.File;
import java.util.function.Supplier;

import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.local_picture_repo.PictureRepository;
import com.metapx.local_picture_repo.impl.DiskFileInformation;

/**
 * A container for picture and metadata repository instances. 
 */
public class CombinedRepository {
  final private Supplier<PictureRepository> pictureRepositorySupplier;
  final private Supplier<MetadataRepository> metadataRepositorySupplier;

  private PictureRepository pictureRepository;
  private MetadataRepository metadataRepository;
  
  public CombinedRepository(Supplier<PictureRepository> pictureRepository, Supplier<MetadataRepository> metadataRepository) {
    this.pictureRepositorySupplier = pictureRepository;
    this.metadataRepositorySupplier = metadataRepository;
  }
  
  public PictureRepository getPictureRepository() {
    if (pictureRepository == null) {
      pictureRepository = pictureRepositorySupplier.get();
    }
    return pictureRepository;
  }

  public MetadataRepository getMetadataRepository() {
    if (metadataRepository == null) {
      metadataRepository = metadataRepositorySupplier.get();
    }
    return metadataRepository;
  }
  
  public RepositoryStatusFileInformation getFile(File file) {
    return new RepositoryStatusFileInformationImpl(this, new DiskFileInformation(file));
  }
}
