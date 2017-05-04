package com.metapx.local_client.cli;

import java.sql.Connection;
import java.util.Optional;

import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.local_client.database.ConnectionFactory;
import com.metapx.local_client.database.DatabaseBuilder;
import com.metapx.local_client.picture_repo.Repository;

public class ClientEnvironment {
  public final Configuration configuration;
  public final Connection connection;
  public final Console console;

  Repository pictureRepository;
  Optional<MetadataRepository> metadataRepository;

  public ClientEnvironment() throws Exception {
    // Setup the environment
    configuration = Configuration.getDefaultConfiguration();
    connection = configureDatabaseConnection(configuration);
    console = new Console.DefaultConsole(configuration);
  }

  public Repository getPictureRepository() {
    if (pictureRepository == null) {
      pictureRepository = new Repository(connection);
    }
    return pictureRepository;
  }

  public Optional<MetadataRepository> getMetadataRepository() {
    if (metadataRepository == null) {
      metadataRepository = new MetadataRepositorySelector(configuration).getDefault();
    }
    return metadataRepository;
  }

  public MetadataRepository getMetadataRepositoryOrThrow() throws Exception {
    final Optional<MetadataRepository> repo = getMetadataRepository();
		if (!repo.isPresent()) {
			throw new Exception("No metadata repository");
		}
    return repo.get();
  }

  private Connection configureDatabaseConnection(Configuration conf) throws Exception {
    if (!conf.getDatabasePath().exists()) {
      DatabaseBuilder.buildFile(conf.getJdbcDatabaseName());
    }
    return ConnectionFactory.newConnection(conf.getJdbcDatabaseName());
  }
}
