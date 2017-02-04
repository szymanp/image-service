package com.metapx.local_client.cli;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.codehaus.plexus.util.DirectoryScanner;

import com.beust.jcommander.*;
import com.metapx.local_client.database.ConnectionFactory;
import com.metapx.local_client.database.DatabaseBuilder;
import com.metapx.local_client.picture_repo.Repository;

public class Client {
  public static void main(String args[]) {
    Client cli = null;

    try {
      cli = new Client(args);
      cli.run();
      cli.conn.commit();
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
          addCommand.run();
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

    public void run() {
      Repository repo = new Repository(conn);

      String[] patternsArray = new String[patterns.size()];
      patterns.toArray(patternsArray);

      DirectoryScanner scanner = new DirectoryScanner();
      scanner.setIncludes(patternsArray);
      scanner.setBasedir(conf.getWorkingDirectory());
      scanner.setCaseSensitive(false);
      scanner.scan();

      Arrays.stream(scanner.getIncludedFiles())
        .forEach(relativePath -> {
          final File targetFile = new File(conf.getWorkingDirectory(), relativePath);
          repo.addFile(targetFile);
        });
    }
  }

}
