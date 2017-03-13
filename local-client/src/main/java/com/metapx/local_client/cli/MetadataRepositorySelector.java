package com.metapx.local_client.cli;

import java.io.File;
import java.util.Optional;

import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.git_metadata.core.MetadataRepositoryHelper;
import com.metapx.git_metadata.core.MetadataRepository.RepositoryException;

public class MetadataRepositorySelector {
  private final Configuration conf;

  public MetadataRepositorySelector(Configuration conf) {
    this.conf = conf;
  }

  public Optional<MetadataRepository> getDefault() {
    final File location = new File(conf.getConfigurationDirectory(), "default.repo");
    final MetadataRepositoryHelper helper = new MetadataRepositoryHelper(location);
    if (helper.exists()) {
      try {
        return Optional.of(new MetadataRepository(location));
      } catch (RepositoryException e) {
        return Optional.empty();
      }
    } else {
      try {
        return Optional.of(helper.create());
      } catch (Exception e) {
        return Optional.empty();
      }
    }
  }
}
