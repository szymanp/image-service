package com.metapx.local_client.cli;

import java.io.File;
import java.util.Map;

public class Configuration {
  private static final String DATABASE_FILE_EXTENSION = "h2.db";

  /** The name of the database file without any extensions. */
  private String databaseFile = "metapx";
  /** The full path and name of the database file. */
  private File databasePath;
  /** Same as `databasePath` but without the extension */
  private String jdbcDatabaseName;
  private File workingDirectory;
  private File confDirectory;
  private String deviceName;

  private static Configuration defaultConfiguration;

  public static Configuration getDefaultConfiguration() {
    if (defaultConfiguration == null) {
      Configuration conf = new Configuration();
      conf.workingDirectory = new File(System.getProperty("user.dir"));
      conf.confDirectory = new File(System.getProperty("user.home") 
        + File.separatorChar 
        + ".metapx");
      conf.jdbcDatabaseName = conf.confDirectory.getAbsolutePath()
        + File.separatorChar 
        + conf.databaseFile;
      conf.databasePath = new File(conf.jdbcDatabaseName + "." + DATABASE_FILE_EXTENSION);
      conf.deviceName = getComputerName();
      
      defaultConfiguration = conf;
    }
    return defaultConfiguration;
  }

  public File getConfigurationDirectory() { return confDirectory; }
  public File getDatabasePath() { return databasePath; }
  public File getWorkingDirectory() { return workingDirectory; }
  public String getJdbcDatabaseName() { return jdbcDatabaseName; }
  public String getDeviceName() { return deviceName; }

  @Override
  public String toString() {
    return "databasePath = " + databasePath + "\n"
         + "workingDirectory = " + workingDirectory + "\n"
         + "confDirectory = " + confDirectory + "\n";
  }
  
  private static String getComputerName() {
    Map<String, String> env = System.getenv();
    if (env.containsKey("COMPUTERNAME"))
      return env.get("COMPUTERNAME");
    else if (env.containsKey("HOSTNAME"))
      return env.get("HOSTNAME");
    else
      return "localhost";
  }
}
