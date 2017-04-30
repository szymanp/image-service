package com.metapx.local_client.cli;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.beust.jcommander.*;
import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.local_client.database.ConnectionFactory;
import com.metapx.local_client.database.DatabaseBuilder;
import com.metapx.local_client.picture_repo.Repository;
import com.metapx.local_client.picture_repo.FileInformation;

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
  Console console;
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
    console = new Console.DefaultConsole(conf);
  }

  void run() throws Exception {
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

    public void run() throws Exception {
      Repository pictureRepo = new Repository(conn);
      Optional<MetadataRepository> metadataRepo = new MetadataRepositorySelector(conf).getDefault();
      if (!metadataRepo.isPresent()) {
        throw new Exception("No metadata repository");
      }
      RepositoryActions repoActions = new RepositoryActions(conf, conn, pictureRepo, metadataRepo.get());

      WildcardMatcher matcher = new WildcardMatcher(patterns);

      matcher.files.stream()
        .forEach(targetFile -> {
          Console.ProcessedFileStatus status = console.startProcessingFile(targetFile);
          FileInformation targetFileInformation = new FileInformation(targetFile);

          if (targetFileInformation.isImage()) {
            try {
              repoActions.addFileAsPicture(targetFileInformation);
              status.success(targetFileInformation);
            } catch (Repository.RepositoryException e) {
              status.fail(e.getMessage());
            } catch (IOException e) {
              e.printStackTrace();
            }
          } else {
            status.fail("Skipping - not an image");
          }
        });

      repoActions.commit();
    }
  }
}
