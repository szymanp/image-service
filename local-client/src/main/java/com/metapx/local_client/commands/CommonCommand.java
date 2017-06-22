package com.metapx.local_client.commands;

import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;

public abstract class CommonCommand implements CommandRunnable {
  @Option(type = OptionType.GLOBAL, name = "--json", description = "JSON output mode")
  public boolean jsonOutput;
}
