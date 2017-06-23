package com.metapx.local_client.cli;

import java.sql.SQLException;
import java.util.Optional;

import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.local_client.combined_repo.CombinedRepository;
import com.metapx.local_picture_repo.PictureRepository;

/**
 * A client environment that replaces certain properties of a parent environment.
 */
public class DerivedEnvironment implements ClientEnvironment {
  public static class DerivedEnvironmentConfiguration {
    public Console console = null;
    
    public static DerivedEnvironmentConfiguration create() {
      return new DerivedEnvironmentConfiguration();
    }
    
    public DerivedEnvironmentConfiguration setConsole(Console console) {
      this.console = console;
      return this;
    }
  }
  
  private final ClientEnvironment parent;
  private final Console console;
  
  public DerivedEnvironment(ClientEnvironment parent, DerivedEnvironmentConfiguration conf) {
    this.parent = parent;
    this.console = conf.console;
  }

  @Override
  public Console getConsole() {
    return console == null ? parent.getConsole() : console;
  }

  @Override
  public Configuration getConfiguration() {
    return parent.getConfiguration();
  }

  @Override
  public PictureRepository getPictureRepository() {
    return parent.getPictureRepository();
  }

  @Override
  public Optional<MetadataRepository> getMetadataRepository() {
    return parent.getMetadataRepository();
  }

  @Override
  public MetadataRepository getMetadataRepositoryOrThrow() throws Exception {
    return parent.getMetadataRepositoryOrThrow();
  }

  @Override
  public CombinedRepository getCombinedRepository() {
    return parent.getCombinedRepository();
  }

  @Override
  public void commit() throws Exception {
    parent.commit();
  }

  @Override
  public void closeConnection() throws SQLException {
    parent.closeConnection();
  }

}
