package com.metapx.local_client.cli;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.beust.jcommander.*;
import com.metapx.local_client.database.ConnectionFactory;
import com.metapx.local_client.database.DatabaseBuilder;

public class Client {
  public static void main(String args[]) {
    Client cli = null;

    try {
      cli = new Client(args);
      cli.run();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (cli != null) {
        try {
          cli.conn.close();
        } catch (SQLException e) {
          // suppress
        }
      }
    }
  }

  Configuration conf;
  Connection conn;
  JCommander jc;

  CommandMain mainCommand;
  CommandAdd addCommand;

  // http://www.jcommander.org/#_more_complex_syntaxes_commands

  Client(String args[]) throws Exception {
    // Setup commands and parse
    mainCommand = new CommandMain();
    jc = new JCommander(mainCommand);

    addCommand = new CommandAdd();
    jc.addCommand("add", addCommand);

    jc.parse(args);

    // Setup the enivornment
    conf = Configuration.getDefaultConfiguration();
    conn = configureDatabaseConnection(conf);
  }

  void run() {
    System.out.println("hello world");

    String command = jc.getParsedCommand();
    if (command == null) {
      jc.usage();
    } else {
      switch(command) {
        case "main":
          if (mainCommand.help) {
            jc.usage();
          }
          break;
        case "add":
          System.out.println(addCommand.patterns);
          break;
        default:
          jc.usage();
      }
    }
  }

  Connection configureDatabaseConnection(Configuration conf) throws Exception {
    if (!conf.getDatabasePath().exists()) {
      DatabaseBuilder.buildFile(conf.getJdbcDatabaseName());
    }
    return ConnectionFactory.newConnection(conf.getJdbcDatabaseName());
  }

  @Parameters(commandDescription = "Generic options")
  private class CommandMain {
    @Parameter(names = "--help", help = true)
    private boolean help;
  }

  @Parameters(commandDescription = "Add file to repository")
  private class CommandAdd {
    @Parameter(description = "File patterns to add to repository")
    List<String> patterns;
  }

}
