package com.metapx.local_client.cli;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.local_client.combined_repo.CombinedRepository;
import com.metapx.local_picture_repo.PictureRepository;
import com.metapx.local_picture_repo.database.ConnectionFactory;
import com.metapx.local_picture_repo.database.DatabaseBuilder;
import com.metapx.local_picture_repo.impl.RepositoryImpl;

public class ClientEnvironment {
  public final Configuration configuration;
  public final Console console;

  private Connection connection;

  RepositoryImpl pictureRepository;
  Optional<MetadataRepository> metadataRepository;

  public ClientEnvironment() {
    // Setup the environment
    configuration = Configuration.getDefaultConfiguration();
    console = new Console.DefaultConsole(configuration);
  }

  public PictureRepository getPictureRepository() {
    if (pictureRepository == null) {
      pictureRepository = new RepositoryImpl(getConnection());
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
  
  public CombinedRepository getCombinedRepository() throws Exception {
    return new CombinedRepository(getPictureRepository(), getMetadataRepositoryOrThrow());
  }

  public void commit() throws Exception {
    if (connection != null) {
      connection.commit();
    }
    if (metadataRepository != null && metadataRepository.isPresent()) {
      metadataRepository.get().commit();
    }
  }
  
  public void closeConnection() throws SQLException {
    if (connection != null) {
      connection.close();
      connection = null;
    }
  }

  private Connection configureDatabaseConnection(Configuration conf) throws Exception {
    if (!conf.getDatabasePath().exists()) {
      DatabaseBuilder.buildFile(conf.getJdbcDatabaseName());
    }
    return ConnectionFactory.newConnection(conf.getJdbcDatabaseName());
  }
  
  private Connection getConnection() {
    if (connection == null) {
      try {
        connection = configureDatabaseConnection(configuration);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return connection;
  }
}
