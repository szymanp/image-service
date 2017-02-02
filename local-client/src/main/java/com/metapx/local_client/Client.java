package com.metapx.local_client;

import java.util.List;

import com.beust.jcommander.*;

public class Client {
  public static void main(String args[]) {
    new Client(args);
  }

  // http://www.jcommander.org/#_more_complex_syntaxes_commands

  Client(String args[]) {
    CommandMain mainCommand = new CommandMain();
    JCommander jc = new JCommander(mainCommand);

    CommandAdd addCommand = new CommandAdd();
    jc.addCommand("add", addCommand);

    jc.parse(args);

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
