package com.metapx.local_client.cli.commands;

public class CommandException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public CommandException(String message) {
    super(message);
  }
}
