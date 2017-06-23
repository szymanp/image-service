package com.metapx.local_client.cli;

import java.sql.SQLException;

import com.github.rvesse.airline.annotations.Cli;
import com.github.rvesse.airline.annotations.Group;
import com.metapx.local_client.commands.*;
import com.metapx.local_client.resources.JsonCommandRunner;
import com.metapx.local_picture_repo.database.ConnectionFactory;
import com.metapx.local_picture_repo.database.DatabaseBuilder;

import io.vertx.core.json.JsonObject;

@Cli(name = "metapx-cli",
     description = "Metapixels client",
     defaultCommand = Help.class,
     commands = { 
       FilesGroup.AddCommand.class,
       FilesGroup.ListCommand.class,
       GroupGroup.CreateCommand.class,
       GroupGroup.ListCommand.class,
       ListCommand.class,
       DaemonCommand.class,
       Help.class },
     groups = {
       @Group(name = "files", description = "Track image files in this repository"),
       @Group(name = "group", description = "Manage picture groups") 
     })
public class Client {
  public static void main(String args[]) {
    disableJooqLogo();

    final com.github.rvesse.airline.Cli<Object> cli = new com.github.rvesse.airline.Cli<Object>(Client.class);
    final Object cmd = cli.parse(args);
    
    if (cmd instanceof CommonCommand && ((CommonCommand) cmd).jsonOutput) {
      runWithJson((CommandRunnable) cmd);
    } else if (cmd instanceof CommandRunnable) {
      run((CommandRunnable) cmd);
    } else if (cmd instanceof Runnable) {
      run((Runnable) cmd);
    } else {
      throw new RuntimeException("Unsupported command type");
    }
  }
  
  private static void runWithJson(CommandRunnable cmd) {
    try {
      configure();

      final JsonObject result = new JsonCommandRunner().run((CommandRunnable) cmd);
      System.out.println(result.encodePrettily());
    } finally {
      ConnectionFactory.SharedConnectionPool.close();
    }
  }
  
  private static void run(CommandRunnable cmd) {
    configure();

    final ClientEnvironment env = new ClientEnvironment();

    try {
      cmd.run(env);
      env.commit();

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        env.closeConnection();
      } catch (SQLException e) {
        // suppress
      }
      
      ConnectionFactory.SharedConnectionPool.close();
    }
  }
  
  private static void run(Runnable cmd) {
    cmd.run();
  }
  
  public static void configure() {
    final Configuration conf = Configuration.getDefaultConfiguration();
    
    ConnectionFactory.SharedConnectionPool.create(() -> {
      if (!conf.getDatabasePath().exists()) {
        try {
          DatabaseBuilder.buildFile(conf.getJdbcDatabaseName());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      return ConnectionFactory.newConnectionPool(conf.getJdbcDatabaseName());
    });
  }
  
  private static void disableJooqLogo() {
    System.setProperty("org.jooq.no-logo", "true");
  }
}
