package com.metapx.local_client.cli.commands;

import com.metapx.local_client.cli.ClientEnvironment;

public interface CommandRunnable {
  public void run(ClientEnvironment env) throws Exception;
}
