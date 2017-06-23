package com.metapx.local_client.cli;

import java.sql.SQLException;
import java.util.Optional;

import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.local_client.combined_repo.CombinedRepository;
import com.metapx.local_picture_repo.PictureRepository;

public interface ClientEnvironment {
  /**
   * @return a new instance of the client environment - with a default configuration.
   */
  public static ClientEnvironment newInstance() {
    return new ClientEnvironmentImpl();
  }
  
  /**
   * @return the currently configured console for printing client messages and results.
   */
  public Console getConsole();

  /**
   * @return the client configuration.
   */
  public Configuration getConfiguration();
  
  /**
   * Returns the system-wide picture repository.
   * This method will establish a connection to the repository, if no such connection exists already.
   * This connection needs to be terminated using the <code>closeConnection()</code> method when
   * no longer needed.
   * @return the system-wide picture repository.
   */
  public PictureRepository getPictureRepository();

  /**
   * @return the currently selected metadata repository, or an empty optional if none is selected.
   */
  public Optional<MetadataRepository> getMetadataRepository();

  /**
   * @return the currently selected metadata repository.
   * @throws Exception if no metadata repository is selected.
   */
  public MetadataRepository getMetadataRepositoryOrThrow() throws Exception;

  /**
   * @return a combined repository representing the system-wide picture repository
   *         and the currently selected metadata repository.
   */
  public CombinedRepository getCombinedRepository();

  /**
   * Commits any pending changes in the picture and metadata repositories.
   */
  public void commit() throws Exception;
  
  /**
   * Closes the connection to the database of the picture repository.
   */
  public void closeConnection() throws SQLException;
}
