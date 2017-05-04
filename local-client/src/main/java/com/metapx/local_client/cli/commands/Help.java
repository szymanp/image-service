package com.metapx.local_client.cli.commands;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.model.GlobalMetadata;
import com.metapx.local_client.cli.ClientEnvironment;

@Command(name = "help", description = "Shows help for a given command")
public class Help implements CommandRunnable {

  @Inject
  private GlobalMetadata<CommandRunnable> global;

  @Arguments(description = "Provides the name of the commands you want to provide help for")
  private List<String> commandNames = new ArrayList<String>();

  public void run(ClientEnvironment env) throws Exception {
    com.github.rvesse.airline.help.Help.help(global, commandNames, false);
  }
}
