package com.metapx.local_client.cli;

import java.sql.SQLException;

import com.github.rvesse.airline.annotations.Cli;
import com.github.rvesse.airline.annotations.Group;
import com.metapx.local_client.cli.commands.*;

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
    ClientEnvironment env = null;

    try {
      final com.github.rvesse.airline.Cli<CommandRunnable> cli = new com.github.rvesse.airline.Cli<CommandRunnable>(Client.class);
      final CommandRunnable cmd = cli.parse(args);
      env = new ClientEnvironment();
      
      cmd.run(env);
      env.commit();

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (env != null) {
        try {
          env.closeConnection();
        } catch (SQLException e) {
          // suppress
        }
      }
    }
  }
  
  private static void disableJooqLogo() {
    System.setProperty("org.jooq.no-logo", "true");
  }
}
