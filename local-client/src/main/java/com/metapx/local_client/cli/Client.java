package com.metapx.local_client.cli;

import java.sql.SQLException;

import com.github.rvesse.airline.annotations.Cli;
import com.metapx.local_client.cli.commands.*;

@Cli(name = "metapx-cli",
     description = "Metapixels client",
     defaultCommand = Help.class,
     commands = { Files.class, Group.class, Help.class })
public class Client {
  public static void main(String args[]) {
    ClientEnvironment env = null;

    try {
      final com.github.rvesse.airline.Cli<CommandRunnable> cli = new com.github.rvesse.airline.Cli<CommandRunnable>(Client.class);
      final CommandRunnable cmd = cli.parse(args);
      env = new ClientEnvironment();
      
      cmd.run(env);
      env.connection.commit();

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (env != null) {
        try {
          env.connection.close();
        } catch (SQLException e) {
          // suppress
        }
      }
    }
  }
}
