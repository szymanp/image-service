package com.metapx.local_repo_server.metadata_repo;

import java.io.File;

import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.git_metadata.core.MetadataRepository.RepositoryException;

import rx.Single;

public class MetadataRepositoryContext {
  final File root;
  final MetadataRepository repo;

  public MetadataRepositoryContext(File root) {
    this.root = root;
    try {
      repo = new MetadataRepository(root);
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
  }

  public Single<MetadataRepository> getMetadataRepository() {
    // TODO
    // A modified implementation of the metadata repository is needed that supports concurrent access.
    return Single.just(repo);
  }
}
