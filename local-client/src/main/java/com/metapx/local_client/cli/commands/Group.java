package com.metapx.local_client.cli.commands;

import java.util.List;

import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.RequireOnlyOne;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.metapx.local_client.cli.ClientEnvironment;

@Command(name = "group", description = "Manage picture groups")
public class Group implements CommandRunnable {

  @Option(name = { "-c", "--create" },
          title = "create",
          description = "Creates a new group")
  @RequireOnlyOne(tag = "action")
  private boolean create = false;

  @Option(name = { "-r", "--remove" },
          title = "remove",
          description = "Removes an existing group")
  @RequireOnlyOne(tag = "action")
  private boolean remove = false;

  @Arguments(title = "group-path")
  @Required
  private List<String> groups;

  public void run(ClientEnvironment env) throws Exception {

  }
}
