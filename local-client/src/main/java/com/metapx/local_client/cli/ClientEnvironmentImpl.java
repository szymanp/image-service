package com.metapx.local_client.cli;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.local_client.combined_repo.CombinedRepository;
import com.metapx.local_picture_repo.PictureRepository;
import com.metapx.local_picture_repo.database.ConnectionFactory;
import com.metapx.local_picture_repo.impl.RepositoryImpl;

public class ClientEnvironmentImpl implements ClientEnvironment {
  private final Configuration configuration;
  private final Console console;

  private Connection connection;

  RepositoryImpl pictureRepository;
  Optional<MetadataRepository> metadataRepository;

  public ClientEnvironmentImpl() {
    // Setup the environment
    configuration = Configuration.getDefaultConfiguration();
    console = new Console.DefaultConsole(configuration);
  }

  public ClientEnvironmentImpl(Console console) {
    this.configuration = Configuration.getDefaultConfiguration();
    this.console = console;
  }

  public Console getConsole() {
    return console;
  }

  public Configuration getConfiguration() {
    return configuration;
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
  
  public CombinedRepository getCombinedRepository() {
    return new CombinedRepository(
      () -> getPictureRepository(), 
      () -> {
        try {
          return getMetadataRepositoryOrThrow();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    );
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
  
  /**
   * Uses the shared connection pool to obtain a single connection to the repository database.
   */
  private Connection getConnection() {
    if (connection == null) {
      try {
        connection = ConnectionFactory.SharedConnectionPool.getConnectionPool().getConnection();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
    return connection;
  }
}
